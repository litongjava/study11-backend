/**
 * 动画播放器公共函数库
 * 从三角函数动画中抽取出的可复用组件
 */

// ===== 音频缓存管理模块 =====
class AudioCacheManager {
    constructor(cacheName = 'tts-audio-v1', expiryDays = 7) {
        this.cacheName = cacheName;
        this.cacheExpiry = expiryDays * 24 * 60 * 60 * 1000;
        this.audioCache = null;
    }

    // 初始化 Cache Storage
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

    // 生成缓存键名
    getCacheKey(text) {
        let hash = 0;
        for (let i = 0; i < text.length; i++) {
            const char = text.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash;
        }
        return `tts-${Math.abs(hash)}`;
    }

    // 检查缓存是否过期
    isCacheExpired(timestamp) {
        return Date.now() - timestamp > this.cacheExpiry;
    }

    // 从Cache Storage获取音频
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

    // 保存音频到Cache Storage
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

    // 合成单个文本的语音
    async synthesizeSpeech(text, showCacheIndicator = null) {
        // 检查缓存
        const cachedAudioUrl = await this.cacheManager.getAudioFromCache(text);
        if (cachedAudioUrl) {
            // 显示缓存指示器
            if (showCacheIndicator) {
                showCacheIndicator();
            }
            return cachedAudioUrl;
        }

        // 缓存中没有，请求TTS服务
        try {
            const encodedText = encodeURIComponent(text);
            const response = await fetch(`${this.apiUrl}${encodedText}`);
            if(response.status===200){
               const blob = await response.blob();
               const audioUrl = URL.createObjectURL(blob);

              // 保存到缓存
              await this.cacheManager.saveAudioToCache(text, blob);

              return audioUrl;            
            }else{
                console.error(`TTS合成失败:`, response.status);
            }

        } catch (error) {
            console.error(`TTS合成失败:`, error);
            throw error;
        }
    }

    // 批量合成多个文本的语音
    async synthesizeMultipleSpeech(texts, onProgress = null, showCacheIndicator = null) {
        const results = [];

        for (let i = 0; i < texts.length; i++) {
            try {
                const audioUrl = await this.synthesizeSpeech(texts[i], showCacheIndicator);
                results.push({success: true, audioUrl, text: texts[i]});

                if (onProgress) {
                    onProgress(i + 1, texts.length);
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
    // 格式化时间显示 (毫秒转 mm:ss)
    static formatTime(milliseconds) {
        const totalSeconds = Math.floor(milliseconds / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    // 计算百分比进度
    static calculateProgress(currentTime, totalTime) {
        return Math.min((currentTime / totalTime) * 100, 100);
    }

    // 根据百分比计算时间
    static calculateTimeFromProgress(percentage, totalTime) {
        return (percentage / 100) * totalTime;
    }
}

// ===== 动画场景播放器 =====
class AnimationPlayer {
    constructor(config = {apiUrl: 'https://javalinux.explanation.fun/tts?input='}) {
        // 基本配置
        this.scenes = config.scenes || [];
        this.audioElements = [];
        this.sceneTimestamps = [];
        this.totalDuration = 0;

        // 播放状态
        this.currentScene = 0;
        this.isPlaying = false;
        this.isMuted = false;
        this.wasPausedByUser = false;
        this.pausedScene = 0;
        this.pausedTime = 0;
        this.isDragging = false;

        // DOM 元素 (需要在初始化时传入)
        this.elements = config.elements || {};

        // TTS 管理器
        this.ttsManager = new TTSManager(config.apiUrl);

        // 绑定方法的this
        this.updateProgress = this.updateProgress.bind(this);
        this.handleProgressClick = this.handleProgressClick.bind(this);
        this.handleMouseMove = this.handleMouseMove.bind(this);
        this.handleMouseUp = this.handleMouseUp.bind(this);
    }

    // 初始化播放器
    async init() {
        await this.ttsManager.init();
        this.initializeAudioElements();
        this.bindEvents();
    }

    // 初始化音频元素
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

    // 预加载所有音频
    async preloadAllAudio(onProgress = null) {
        const {loading, playBtn, cacheIndicator} = this.elements;

        // 显示加载状态
        if (loading) loading.classList.add('active');
        if (playBtn) playBtn.disabled = true;

        // 缓存指示器显示函数
        const showCacheIndicator = () => {
            if (cacheIndicator) {
                cacheIndicator.classList.add('active');
                setTimeout(() => {
                    cacheIndicator.classList.remove('active');
                }, 2000);
            }
        };

        // 合成所有语音
        const texts = this.scenes.map(scene => scene.subtitle);
        const results = await this.ttsManager.synthesizeMultipleSpeech(
            texts,
            onProgress,
            showCacheIndicator
        );

        // 设置音频源并获取时长
        for (let i = 0; i < results.length; i++) {
            const result = results[i];
            if (result.success) {
                this.audioElements[i].src = result.audioUrl;
                // 等待音频加载完成以获取时长
                await new Promise((resolve) => {
                    this.audioElements[i].onloadedmetadata = () => {
                        this.scenes[i].duration = this.audioElements[i].duration * 1000;
                        resolve();
                    };
                });
            } else {
                // 使用默认持续时间
                this.scenes[i].duration = 5000;
            }
        }

        // 计算时间戳和总时长
        this.calculateTimestamps();

        // 更新时间显示
        this.updateTimeDisplay(0, this.totalDuration);

        // 隐藏加载状态
        if (loading) loading.classList.remove('active');
        if (playBtn) playBtn.disabled = false;
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
        return p?.allowsFeature?.('autoplay') ?? true; // 不支持 API 时默认 true
    }


    async playWithErrorHandling() {
        const embedded = this.inIframe();
        const policyAllows = this.iframeAutoplayAllowedByPolicy();
        if (!embedded && policyAllows) {
            try {
                await this.play();
                return true;
            } catch (error) {
                if (error.name === 'NotAllowedError') {
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

    // 计算每个场景的时间戳
    calculateTimestamps() {
        this.sceneTimestamps = [];
        let accumulatedTime = 0;

        this.scenes.forEach((scene) => {
            this.sceneTimestamps.push(accumulatedTime);
            accumulatedTime += scene.duration || 5000;
        });

        this.totalDuration = accumulatedTime;
    }

    // 更新时间显示
    updateTimeDisplay(currentTime, totalTime) {
        const {timeDisplay} = this.elements;
        if (timeDisplay) {
            const currentTimeStr = TimeUtils.formatTime(currentTime);
            const totalTimeStr = TimeUtils.formatTime(totalTime);
            timeDisplay.textContent = `${currentTimeStr} / ${totalTimeStr}`;
        }
    }

    // 更新进度条
    updateProgress() {
        if (!this.isPlaying) return;

        const currentAudio = this.audioElements[this.currentScene];
        if (!currentAudio) return;

        // 计算当前时间点
        const sceneStartTime = this.sceneTimestamps[this.currentScene];
        const currentTime = sceneStartTime + (currentAudio.currentTime * 1000);
        const progress = TimeUtils.calculateProgress(currentTime, this.totalDuration);

        // 更新进度条UI
        const {progressFill, progressHandle} = this.elements;
        if (progressFill) progressFill.style.width = progress + '%';
        if (progressHandle) progressHandle.style.left = progress + '%';

        // 更新时间显示
        this.updateTimeDisplay(currentTime, this.totalDuration);

        if (progress >= 100) {
            this.stop();
        } else {
            requestAnimationFrame(this.updateProgress);
        }
    }

    // 播放指定场景
    playScene(sceneIndex) {
        if (sceneIndex >= this.scenes.length) {
            this.stop();
            return;
        }

        // 停止当前音频
        if (this.currentScene < this.audioElements.length && this.audioElements[this.currentScene]) {
            this.audioElements[this.currentScene].pause();
        }

        this.currentScene = sceneIndex;
        const scene = this.scenes[sceneIndex];
        const audio = this.audioElements[sceneIndex];

        // 更新字幕和执行场景动画
        const {subtitle} = this.elements;
        if (subtitle) {
            subtitle.textContent = scene.subtitle;
            subtitle.classList.add('active');
        }

        if (scene.action) {
            scene.action();
        }

        // 播放音频
        if (audio && !this.isMuted) {
            audio.currentTime = 0;
            audio.play().catch(e => {
                console.log('音频播放失败:', e)
                this.showInteractionRequired();
            });

            // 音频结束时播放下一个场景
            audio.onended = () => {
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            };
        } else {
            // 如果静音或没有音频，使用定时器
            setTimeout(() => {
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            }, scene.duration || 5000);
        }
    }

    // 开始播放
    play() {
        if (this.isPlaying) return;

        this.isPlaying = true;

        // 更新播放按钮
        const {playIcon} = this.elements;
        if (playIcon) playIcon.textContent = '⏸';

        if (this.wasPausedByUser && this.pausedScene < this.scenes.length) {
            // 从暂停状态恢复
            this.resumeFromPause();
        } else {
            // 从头开始播放
            this.currentScene = 0;
            this.playScene(0);
        }

        this.updateProgress();
    }

    // 从暂停状态恢复播放
    resumeFromPause() {
        const scene = this.scenes[this.pausedScene];
        const audio = this.audioElements[this.pausedScene];

        // 更新UI
        const {subtitle} = this.elements;
        if (subtitle) {
            subtitle.textContent = scene.subtitle;
            subtitle.classList.add('active');
        }

        if (scene.action) {
            scene.action();
        }

        this.currentScene = this.pausedScene;

        // 播放音频（从暂停位置继续）
        if (audio && !this.isMuted) {
            audio.currentTime = this.pausedTime / 1000;
            audio.play().catch(e => console.log('音频播放失败:', e));

            audio.onended = () => {
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            };
        } else {
            // 使用定时器
            setTimeout(() => {
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            }, (scene.duration || 5000) - this.pausedTime);
        }

        this.wasPausedByUser = false;
    }

    // 暂停播放
    pause() {
        this.isPlaying = false;
        this.wasPausedByUser = true;

        // 更新播放按钮
        const {playIcon} = this.elements;
        if (playIcon) playIcon.textContent = '▶';

        // 保存当前播放状态
        if (this.currentScene < this.audioElements.length && this.audioElements[this.currentScene]) {
            const audio = this.audioElements[this.currentScene];
            this.pausedScene = this.currentScene;
            this.pausedTime = audio.currentTime * 1000;
            audio.pause();
        }
    }

    // 停止播放
    stop() {
        this.pause();
        this.currentScene = 0;
        this.pausedScene = 0;
        this.pausedTime = 0;
        this.wasPausedByUser = false;

        // 重置进度条
        const {progressFill, progressHandle, subtitle} = this.elements;
        if (progressFill) progressFill.style.width = '0%';
        if (progressHandle) progressHandle.style.left = '0%';
        if (subtitle) subtitle.textContent = '';

        // 重置时间显示
        this.updateTimeDisplay(0, this.totalDuration);
    }

    // 切换静音
    toggleMute() {
        this.isMuted = !this.isMuted;

        // 更新静音按钮
        const {muteIcon} = this.elements;
        if (muteIcon) muteIcon.textContent = this.isMuted ? '🔇' : '🔊';

        // 设置所有音频元素的静音状态
        this.audioElements.forEach(audio => {
            audio.muted = this.isMuted;
        });
    }

    // 处理进度条点击
    handleProgressClick(e) {
        const {progressBar} = this.elements;
        if (!progressBar) return;

        const rect = progressBar.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const percentage = Math.max(0, Math.min(100, (x / rect.width) * 100));

        this.seekToProgress(percentage);
    }

    // 跳转到指定进度
    seekToProgress(percentage) {
        const {progressFill, progressHandle} = this.elements;
        if (progressFill) progressFill.style.width = percentage + '%';
        if (progressHandle) progressHandle.style.left = percentage + '%';

        // 计算对应的时间点
        const targetTime = TimeUtils.calculateTimeFromProgress(percentage, this.totalDuration);
        this.updateTimeDisplay(targetTime, this.totalDuration);

        // 找到对应的场景
        let targetScene = 0;
        for (let i = 0; i < this.sceneTimestamps.length; i++) {
            if (targetTime >= this.sceneTimestamps[i]) {
                targetScene = i;
            } else {
                break;
            }
        }

        // 更新状态
        this.pausedScene = targetScene;
        this.pausedTime = targetTime - this.sceneTimestamps[targetScene];

        if (this.isPlaying) {
            this.jumpToScene(targetScene, this.pausedTime);
        } else if (this.wasPausedByUser) {
            this.updateSceneDisplay(targetScene);
        }
    }

    // 跳转到指定场景和时间
    jumpToScene(sceneIndex, timeOffset) {
        // 停止当前音频
        if (this.currentScene < this.audioElements.length && this.audioElements[this.currentScene]) {
            this.audioElements[this.currentScene].pause();
        }

        this.currentScene = sceneIndex;
        const scene = this.scenes[sceneIndex];
        const audio = this.audioElements[sceneIndex];

        // 更新UI
        const {subtitle} = this.elements;
        if (subtitle) {
            subtitle.textContent = scene.subtitle;
            subtitle.classList.add('active');
        }

        if (scene.action) {
            scene.action();
        }

        // 播放音频（从指定时间点开始）
        if (audio && !this.isMuted) {
            audio.currentTime = timeOffset / 1000;
            audio.play().catch(e => console.log('音频播放失败:', e));

            audio.onended = () => {
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            };
        } else {
            // 使用定时器
            setTimeout(() => {
                if (subtitle) subtitle.classList.remove('active');
                this.playScene(this.currentScene + 1);
            }, (scene.duration || 5000) - timeOffset);
        }
    }

    // 更新场景显示（暂停状态下）
    updateSceneDisplay(sceneIndex) {
        this.currentScene = sceneIndex;
        const scene = this.scenes[sceneIndex];

        const {subtitle} = this.elements;
        if (subtitle) {
            subtitle.textContent = scene.subtitle;
            subtitle.classList.add('active');
        }

        if (scene.action) {
            scene.action();
        }
    }

    // 鼠标移动事件处理
    handleMouseMove(e) {
        if (this.isDragging) {
            this.handleProgressClick(e);
        }
    }

    // 鼠标释放事件处理
    handleMouseUp() {
        if (this.isDragging) {
            this.isDragging = false;
        }
    }

    // 绑定事件监听
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

        // 全局事件
        document.addEventListener('mousemove', this.handleMouseMove);
        document.addEventListener('mouseup', this.handleMouseUp);
    }

    // 销毁播放器，清理事件监听
    destroy() {
        document.removeEventListener('mousemove', this.handleMouseMove);
        document.removeEventListener('mouseup', this.handleMouseUp);

        // 停止所有音频
        this.audioElements.forEach(audio => {
            audio.pause();
            if (audio.src.startsWith('blob:')) {
                URL.revokeObjectURL(audio.src);
            }
        });
    }
}

// 导出所有类和工具函数
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        AudioCacheManager,
        TTSManager,
        TimeUtils,
        AnimationPlayer
    };
} else if (typeof window !== 'undefined') {
    window.AnimationUtils = {
        AudioCacheManager,
        TTSManager,
        TimeUtils,
        AnimationPlayer
    };
}