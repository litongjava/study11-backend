/**
 * 动画播放器公共函数库 - 重构版
 * 彻底解决3D场景初始化和音频播放逻辑问题
 */

// ===== 音频缓存管理模块 =====
class AudioCacheManager {
    constructor(cacheName = 'tts-audio-v1', expiryDays = 7) {
        this.cacheName = cacheName;
        this.cacheExpiry = expiryDays * 24 * 60 * 60 * 1000;
        this.audioCache = null;
    }

    async init() {
        try {
            this.audioCache = await caches.open(this.cacheName);
            console.log('Cache Storage 初始化成功');
            return true;
        } catch (error) {
            console.error('Cache Storage 初始化失败:', error);
            this.audioCache = null;
            return false;
        }
    }

    getCacheKey(text) {
        let hash = 0;
        for (let i = 0; i < text.length; i++) {
            const char = text.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash;
        }
        return `tts-${Math.abs(hash)}`;
    }

    isCacheExpired(timestamp) {
        return Date.now() - timestamp > this.cacheExpiry;
    }

    async getAudioFromCache(text) {
        if (!this.audioCache) return null;

        try {
            const cacheKey = this.getCacheKey(text);
            const response = await this.audioCache.match(cacheKey);

            if (response) {
                const timestamp = response.headers.get('X-Cache-Timestamp');
                if (timestamp && this.isCacheExpired(parseInt(timestamp))) {
                    await this.audioCache.delete(cacheKey);
                    return null;
                }

                const blob = await response.blob();
                return URL.createObjectURL(blob);
            }
        } catch (error) {
            console.error('从Cache Storage获取音频失败:', error);
        }

        return null;
    }

    async saveAudioToCache(text, blob) {
        if (!this.audioCache) return;

        try {
            const cacheKey = this.getCacheKey(text);
            const response = new Response(blob, {
                headers: {
                    'Content-Type': blob.type || 'audio/mpeg',
                    'X-Cache-Timestamp': Date.now().toString()
                }
            });

            await this.audioCache.put(cacheKey, response);
            console.log('音频已保存到Cache Storage');
        } catch (error) {
            console.error('保存音频到Cache Storage失败:', error);
        }
    }
}

// ===== TTS 语音合成模块 =====
class TTSManager {
    constructor(apiUrl = 'https://javalinux.explanation.fun/tts?input=') {
        this.apiUrl = apiUrl;
        this.cacheManager = new AudioCacheManager();
    }

    async init() {
        return await this.cacheManager.init();
    }

    async synthesizeSpeech(text, showCacheIndicator = null) {
        const cachedAudioUrl = await this.cacheManager.getAudioFromCache(text);
        if (cachedAudioUrl) {
            if (showCacheIndicator) {
                showCacheIndicator();
            }
            return cachedAudioUrl;
        }

        try {
            const encodedText = encodeURIComponent(text);
            const response = await fetch(`${this.apiUrl}${encodedText}`);
            if (response.status === 200) {
                const blob = await response.blob();
                const audioUrl = URL.createObjectURL(blob);
                await this.cacheManager.saveAudioToCache(text, blob);
                return audioUrl;
            } else {
                console.error(`TTS合成失败:`, response.status);
            }
        } catch (error) {
            console.error(`TTS合成失败:`, error);
            throw error;
        }
    }

    async synthesizeMultipleSpeech(texts, onProgress = null, showCacheIndicator = null) {
        const results = [];

        for (let i = 0; i < texts.length; i++) {
            try {
                const audioUrl = await this.synthesizeSpeech(texts[i], showCacheIndicator);
                if (audioUrl) {
                    results.push({success: true, audioUrl, text: texts[i]});
                    if (onProgress) {
                        onProgress(i + 1, texts.length);
                    }
                }
            } catch (error) {
                results.push({success: false, error, text: texts[i]});
                console.error(`合成第 ${i} 个文本失败:`, error);
            }
        }

        return results;
    }
}

// ===== 时间格式化工具函数 =====
class TimeUtils {
    static formatTime(milliseconds) {
        const totalSeconds = Math.floor(milliseconds / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    static calculateProgress(currentTime, totalTime) {
        return Math.min((currentTime / totalTime) * 100, 100);
    }

    static calculateTimeFromProgress(percentage, totalTime) {
        return (percentage / 100) * totalTime;
    }
}

// ===== 动画场景播放器 - 重构版 =====
class AnimationPlayer {
    constructor(config = {}) {
        this.scenes = config.scenes || [];
        this.audioElements = [];
        this.sceneTimestamps = [];
        this.totalDuration = 0;

        this.currentScene = 0;
        this.isPlaying = false;
        this.isMuted = false;
        this.isDragging = false;

        this.elements = config.elements || {};
        this.ttsManager = new TTSManager(config.apiUrl);

        this.updateProgress = this.updateProgress.bind(this);
        this.handleProgressClick = this.handleProgressClick.bind(this);
        this.handleMouseMove = this.handleMouseMove.bind(this);
        this.handleMouseUp = this.handleMouseUp.bind(this);
    }

    async init() {
        await this.ttsManager.init();
        this.initializeAudioElements();
        this.bindEvents();
    }

    initializeAudioElements() {
        const {audioContainer} = this.elements;
        if (audioContainer) {
            audioContainer.innerHTML = '';
        }

        this.audioElements = [];

        this.scenes.forEach((scene, index) => {
            const audio = document.createElement('audio');
            audio.id = `audio-${index}`;
            audio.preload = 'auto';
            if (audioContainer) {
                audioContainer.appendChild(audio);
            }
            this.audioElements.push(audio);
        });
    }

    waitForMetadata(audio, timeoutMs = 5000) {
        return new Promise((resolve, reject) => {
            if ((audio.readyState >= 1 && isFinite(audio.duration)) || audio.duration > 0) return resolve();
            let to;
            const done = () => {
                cleanup();
                resolve();
            };
            const fail = (e) => {
                cleanup();
                reject(e || new Error('metadata error'));
            };
            const cleanup = () => {
                audio.removeEventListener('loadedmetadata', done);
                audio.removeEventListener('error', fail);
                clearTimeout(to);
            };
            audio.addEventListener('loadedmetadata', done, {once: true});
            audio.addEventListener('error', fail, {once: true});
            audio.load();
            to = setTimeout(() => fail(new Error('metadata timeout')), timeoutMs);
        });
    }

    async preloadAllAudio(onProgress = null) {
        const {loading, playBtn, cacheIndicator} = this.elements;

        if (loading) loading.classList.add('active');
        if (playBtn) playBtn.disabled = true;

        try {
            const showCacheIndicator = () => {
                if (cacheIndicator) {
                    cacheIndicator.classList.add('active');
                    setTimeout(() => {
                        cacheIndicator.classList.remove('active');
                    }, 2000);
                }
            };

            const texts = this.scenes.map(scene => scene.subtitle);
            const results = await this.ttsManager.synthesizeMultipleSpeech(
                texts,
                onProgress,
                showCacheIndicator
            );

            for (let i = 0; i < results.length; i++) {
                const r = results[i];
                if (!r.success) {
                    this.scenes[i].duration = 5000;
                    continue;
                }

                const audio = this.audioElements[i];
                const wait = this.waitForMetadata(audio, 5000);
                audio.src = r.audioUrl;
                try {
                    await wait;
                    const d = audio.duration;
                    this.scenes[i].duration = (isFinite(d) && d > 0 ? d : 5) * 1000;
                } catch {
                    this.scenes[i].duration = 5000;
                }
            }

            this.calculateTimestamps();
            this.updateTimeDisplay(0, this.totalDuration);

        } finally {
            if (loading) loading.classList.remove('active');
            if (playBtn) playBtn.disabled = false;
        }
    }

    calculateTimestamps() {
        this.sceneTimestamps = [];
        let accumulatedTime = 0;

        this.scenes.forEach((scene) => {
            this.sceneTimestamps.push(accumulatedTime);
            accumulatedTime += scene.duration || 5000;
        });

        this.totalDuration = accumulatedTime;
    }

    updateTimeDisplay(currentTime, totalTime) {
        const {timeDisplay} = this.elements;
        if (timeDisplay) {
            const currentTimeStr = TimeUtils.formatTime(currentTime);
            const totalTimeStr = TimeUtils.formatTime(totalTime);
            timeDisplay.textContent = `${currentTimeStr} / ${totalTimeStr}`;
        }
    }

    updateProgress() {
        if (!this.isPlaying) return;

        const currentAudio = this.audioElements[this.currentScene];
        if (!currentAudio) return;

        const sceneStartTime = this.sceneTimestamps[this.currentScene];
        const currentTime = sceneStartTime + (currentAudio.currentTime * 1000);
        const progress = TimeUtils.calculateProgress(currentTime, this.totalDuration);

        const {progressFill, progressHandle} = this.elements;
        if (progressFill) progressFill.style.width = progress + '%';
        if (progressHandle) progressHandle.style.left = progress + '%';

        this.updateTimeDisplay(currentTime, this.totalDuration);

        if (progress >= 100) {
            this.stop();
        } else {
            requestAnimationFrame(this.updateProgress);
        }
    }

    // ✅ 核心方法:切换场景显示(不涉及音频播放)
    switchToScene(sceneIndex) {
        if (sceneIndex < 0 || sceneIndex >= this.scenes.length) return;

        this.currentScene = sceneIndex;
        const scene = this.scenes[sceneIndex];

        // 更新字幕
        const {subtitle} = this.elements;
        if (subtitle) {
            subtitle.textContent = scene.subtitle;
            subtitle.classList.add('active');
        }

        // 执行场景动作(SVG显示或3D初始化)
        if (scene.action) {
            scene.action();
        }

        // 高亮场景按钮
        this.highlightActiveSceneButton(sceneIndex);
    }

    // ✅ 播放指定场景(包含音频)
    playScene(sceneIndex) {
        if (sceneIndex >= this.scenes.length) {
            this.stop();
            return;
        }

        // 停止当前音频
        if (this.currentScene < this.audioElements.length && this.audioElements[this.currentScene]) {
            this.audioElements[this.currentScene].pause();
        }

        // 切换场景显示
        this.switchToScene(sceneIndex);

        const scene = this.scenes[sceneIndex];
        const audio = this.audioElements[sceneIndex];

        // 播放音频
        if (audio && !this.isMuted) {
            audio.currentTime = 0;
            audio.play().catch(e => {
                console.log('音频播放失败:', e);
                this.showInteractionRequired();
            });

            audio.onended = () => {
                const {subtitle} = this.elements;
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            };
        } else {
            setTimeout(() => {
                const {subtitle} = this.elements;
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            }, scene.duration || 5000);
        }
    }

    // ✅ 跳转到指定场景和时间点
    jumpToScene(sceneIndex, timeOffset = 0) {
        if (this.currentScene < this.audioElements.length && this.audioElements[this.currentScene]) {
            this.audioElements[this.currentScene].pause();
        }

        this.switchToScene(sceneIndex);

        const scene = this.scenes[sceneIndex];
        const audio = this.audioElements[sceneIndex];

        if (audio && !this.isMuted) {
            audio.currentTime = timeOffset / 1000;
            audio.play().catch(e => {
                console.log('音频播放失败:', e);
                this.showInteractionRequired();
            });

            audio.onended = () => {
                const {subtitle} = this.elements;
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            };
        } else {
            setTimeout(() => {
                const {subtitle} = this.elements;
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            }, (scene.duration || 5000) - timeOffset);
        }
    }

    inIframe() {
        try {
            return window.self !== window.top;
        } catch (_) {
            return true;
        }
    }

    iframeAutoplayAllowedByPolicy() {
        const p = document.permissionsPolicy || document.featurePolicy;
        return p?.allowsFeature?.('autoplay') ?? true;
    }

    async playWithErrorHandling() {
        const embedded = this.inIframe();
        const policyAllows = this.iframeAutoplayAllowedByPolicy();

        this.highlightActiveSceneButton(this.currentScene || 0);

        if (!embedded && policyAllows) {
            try {
                await this.play();
                return true;
            } catch (error) {
                if (error?.name === 'NotAllowedError') {
                    console.log('需要用户交互才能播放音频');
                    this.showInteractionRequired();
                    return false;
                } else {
                    console.error('播放失败:', error);
                    throw error;
                }
            }
        } else {
            this.showInteractionRequired();
        }
    }

    showInteractionRequired() {
        const message = document.createElement('div');
        message.innerHTML = `
        <div style="text-align: center; padding: 20px; background: rgba(231, 76, 60, 0.1); border: 2px solid #e74c3c; border-radius: 10px; margin: 20px;">
            <p style="color: #e74c3c; font-size: 1.1rem; margin-bottom: 15px;">
                🔊 浏览器需要用户交互才能播放音频
            </p>
            <button id="manualStartBtn" style="padding: 10px 20px; background: #3498db; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 1rem;">
                点击开始播放
            </button>
        </div>
        `;

        const container = document.querySelector('.animation-container');
        if (container) {
            container.insertBefore(message, container.firstChild);

            document.getElementById('manualStartBtn').addEventListener('click', () => {
                message.remove();
                try {
                    this.isPlaying = false;
                    this.play();
                } catch (e) {
                    console.error('播放失败:', e);
                }
            });
        }
    }

    play() {
        if (this.isPlaying) return;

        this.isPlaying = true;

        const {playIcon} = this.elements;
        if (playIcon) playIcon.textContent = '⏸';

        this.currentScene = 0;
        this.playScene(0);
        this.updateProgress();
    }

    pause() {
        this.isPlaying = false;

        const {playIcon} = this.elements;
        if (playIcon) playIcon.textContent = '▶';

        if (this.currentScene < this.audioElements.length && this.audioElements[this.currentScene]) {
            const audio = this.audioElements[this.currentScene];
            audio.pause();
        }
    }

    stop() {
        this.pause();
        this.currentScene = 0;

        const {progressFill, progressHandle, subtitle} = this.elements;
        if (progressFill) progressFill.style.width = '0%';
        if (progressHandle) progressHandle.style.left = '0%';
        if (subtitle) subtitle.textContent = '';

        this.updateTimeDisplay(0, this.totalDuration);
    }

    toggleMute() {
        this.isMuted = !this.isMuted;

        const {muteIcon} = this.elements;
        if (muteIcon) muteIcon.textContent = this.isMuted ? '🔇' : '🔊';

        this.audioElements.forEach(audio => {
            audio.muted = this.isMuted;
        });
    }

    handleProgressClick(e) {
        const {progressBar} = this.elements;
        if (!progressBar) return;

        const rect = progressBar.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const percentage = Math.max(0, Math.min(100, (x / rect.width) * 100));

        this.seekToProgress(percentage);
    }

    seekToProgress(percentage) {
        const {progressFill, progressHandle} = this.elements;
        if (progressFill) progressFill.style.width = percentage + '%';
        if (progressHandle) progressHandle.style.left = percentage + '%';

        const targetTime = TimeUtils.calculateTimeFromProgress(percentage, this.totalDuration);
        this.updateTimeDisplay(targetTime, this.totalDuration);

        let targetScene = 0;
        for (let i = 0; i < this.sceneTimestamps.length; i++) {
            if (targetTime >= this.sceneTimestamps[i]) {
                targetScene = i;
            } else {
                break;
            }
        }

        const timeOffset = targetTime - this.sceneTimestamps[targetScene];

        if (this.isPlaying) {
            this.jumpToScene(targetScene, timeOffset);
        } else {
            this.switchToScene(targetScene);
        }
    }

    handleMouseMove(e) {
        if (this.isDragging) {
            this.handleProgressClick(e);
        }
    }

    handleMouseUp() {
        if (this.isDragging) {
            this.isDragging = false;
        }
    }

    bindEvents() {
        const {playBtn, muteBtn, progressBar, progressHandle} = this.elements;

        if (playBtn) {
            playBtn.addEventListener('click', () => {
                if (this.isPlaying) {
                    this.pause();
                } else {
                    this.play();
                }
            });
        }

        if (muteBtn) {
            muteBtn.addEventListener('click', () => this.toggleMute());
        }

        if (progressBar) {
            progressBar.addEventListener('click', this.handleProgressClick);
        }

        if (progressHandle) {
            progressHandle.addEventListener('mousedown', () => {
                this.isDragging = true;
            });
        }

        document.addEventListener('mousemove', this.handleMouseMove);
        document.addEventListener('mouseup', this.handleMouseUp);
    }

    // ✅ 设置场景按钮(重构版)
    setSceneButtons(container) {
        this.sceneButtonsContainer = container || null;
        if (!this.sceneButtonsContainer) return;

        this.sceneButtonsContainer.innerHTML = '';
        this.scenes.forEach((scene, idx) => {
            const btn = document.createElement('button');
            btn.className = 'scene-btn';
            btn.textContent = String(idx + 1);

            btn.addEventListener('click', () => {
                if (this.isPlaying) {
                    // 播放中:跳转并继续播放
                    this.jumpToScene(idx, 0);
                } else {
                    // 暂停中:只切换画面,不播放音频
                    this.switchToScene(idx);
                }

                // 同步进度条
                const t0 = this.sceneTimestamps?.[idx] ?? 0;
                const percent = this.totalDuration ? Math.min(100, (t0 / this.totalDuration) * 100) : 0;
                this.elements.progressFill && (this.elements.progressFill.style.width = percent + '%');
                this.elements.progressHandle && (this.elements.progressHandle.style.left = percent + '%');
                this.updateTimeDisplay(t0, this.totalDuration);
            });

            this.sceneButtonsContainer.appendChild(btn);
        });

        this.highlightActiveSceneButton(0);
    }

    highlightActiveSceneButton(index) {
        if (!this.sceneButtonsContainer) return;
        const buttons = this.sceneButtonsContainer.querySelectorAll('.scene-btn');
        buttons.forEach((b, i) => b.classList.toggle('active', i === index));
    }

    destroy() {
        document.removeEventListener('mousemove', this.handleMouseMove);
        document.removeEventListener('mouseup', this.handleMouseUp);

        this.audioElements.forEach(audio => {
            audio.pause();
            if (audio.src.startsWith('blob:')) {
                URL.revokeObjectURL(audio.src);
            }
        });
    }
}

// ===== 扩展 AnimationPlayer 支持 3D 场景和 GeoGebra =====
class AnimationPlayerWith3D extends AnimationPlayer {
    constructor(config) {
        super(config);
        this.threejsScenes = new Map();
        this.activeAnimations = new Map();
        this.geogebraApplets = new Map(); // 存储 GeoGebra 实例
    }

    // ✅ 初始化 3D 场景
    init3DScene(sceneIndex, canvasId, setupCallback) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) {
            console.error(`❌ Canvas ${canvasId} not found`);
            return null;
        }

        if (this.threejsScenes.has(sceneIndex)) {
            console.log(`♻️ 3D场景 ${sceneIndex} 已存在,跳过初始化`);
            return this.threejsScenes.get(sceneIndex);
        }

        console.log(`🎨 初始化 3D 场景 ${sceneIndex}`);

        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(
            75,
            canvas.width / canvas.height,
            0.1,
            1000
        );
        const renderer = new THREE.WebGLRenderer({
            canvas,
            antialias: true,
            alpha: true
        });

        renderer.setSize(canvas.width, canvas.height);
        camera.position.z = 5;

        const ambientLight = new THREE.AmbientLight(0x404040);
        scene.add(ambientLight);

        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
        directionalLight.position.set(5, 5, 5);
        scene.add(directionalLight);

        const threeSetup = {
            scene,
            camera,
            renderer,
            objects: {}
        };

        if (setupCallback) {
            setupCallback(threeSetup);
        }

        this.threejsScenes.set(sceneIndex, threeSetup);

        return threeSetup;
    }

    // ✅ 启动 3D 动画
    start3DAnimation(sceneIndex, animateCallback) {
        const threeSetup = this.threejsScenes.get(sceneIndex);
        if (!threeSetup) {
            console.error(`❌ 3D场景 ${sceneIndex} 未初始化`);
            return;
        }

        this.stop3DAnimation(sceneIndex);

        console.log(`▶️ 启动 3D 动画 ${sceneIndex}`);

        const animate = () => {
            const animationId = requestAnimationFrame(animate);
            this.activeAnimations.set(sceneIndex, animationId);

            if (animateCallback) {
                animateCallback(threeSetup);
            }

            threeSetup.renderer.render(threeSetup.scene, threeSetup.camera);
        };

        animate();
    }

    // ✅ 停止 3D 动画
    stop3DAnimation(sceneIndex) {
        const animationId = this.activeAnimations.get(sceneIndex);
        if (animationId) {
            cancelAnimationFrame(animationId);
            this.activeAnimations.delete(sceneIndex);
            console.log(`⏸ 停止 3D 动画 ${sceneIndex}`);
        }
    }

    // ✅ 清理 3D 场景资源
    dispose3DScene(sceneIndex) {
        this.stop3DAnimation(sceneIndex);

        const threeSetup = this.threejsScenes.get(sceneIndex);
        if (threeSetup) {
            threeSetup.scene.traverse((object) => {
                if (object.geometry) {
                    object.geometry.dispose();
                }
                if (object.material) {
                    if (Array.isArray(object.material)) {
                        object.material.forEach(material => material.dispose());
                    } else {
                        object.material.dispose();
                    }
                }
            });

            threeSetup.renderer.dispose();
            this.threejsScenes.delete(sceneIndex);
            console.log(`🗑️ 清理 3D 场景 ${sceneIndex}`);
        }
    }

    // ✅ 重写 switchToScene: 处理 3D 场景和 GeoGebra 初始化
    switchToScene(sceneIndex) {
        if (sceneIndex < 0 || sceneIndex >= this.scenes.length) return;

        // 停止所有其他 3D 动画
        this.activeAnimations.forEach((_, index) => {
            if (index !== sceneIndex) {
                this.stop3DAnimation(index);
            }
        });

        // 调用父类方法(更新字幕、执行action)
        super.switchToScene(sceneIndex);

        const scene = this.scenes[sceneIndex];

        // 处理 3D 场景
        if (scene.is3D) {
            const canvasId = scene.canvasId || 'canvas3d';

            // 如果未初始化,则初始化
            if (!this.threejsScenes.has(sceneIndex)) {
                if (scene.setup3D) {
                    this.init3DScene(sceneIndex, canvasId, scene.setup3D);
                } else {
                    console.error(`❌ 场景 ${sceneIndex} 标记为 3D 但缺少 setup3D 方法`);
                    return;
                }
            }

            // 启动 3D 动画
            if (scene.animate3D) {
                this.start3DAnimation(sceneIndex, scene.animate3D);
            }
        }

        // 处理 GeoGebra 场景
        if (scene.isGeoGebra) {
            if (!this.geogebraApplets.has(sceneIndex)) {
                if (scene.setupGeoGebra) {
                    console.log(`🎨 初始化 GeoGebra 场景 ${sceneIndex}`);
                    scene.setupGeoGebra();
                    this.geogebraApplets.set(sceneIndex, true);
                } else {
                    console.error(`❌ 场景 ${sceneIndex} 标记为 GeoGebra 但缺少 setupGeoGebra 方法`);
                }
            }
        }
    }

    destroy() {
        this.threejsScenes.forEach((_, index) => {
            this.dispose3DScene(index);
        });

        // 清理 GeoGebra 实例
        this.geogebraApplets.clear();

        super.destroy();
    }
}

// ===== 导出模块 =====
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        AudioCacheManager,
        TTSManager,
        TimeUtils,
        AnimationPlayer,
        AnimationPlayerWith3D
    };
} else if (typeof window !== 'undefined') {
    window.AnimationUtils = {
        AudioCacheManager,
        TTSManager,
        TimeUtils,
        AnimationPlayer,
        AnimationPlayerWith3D
    };
}