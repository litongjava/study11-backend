// 动画场景数据
const scenes = [
    {
        title: "Introduction to Newton's First Law",
        subtitle: "Newton's First Law states: An object at rest stays at rest and an object in motion stays in motion with the same speed and in the same direction unless acted upon by an unbalanced force.",
        action: () => {
            // 显示场景1，隐藏其他场景
            for (let i = 1; i <= 6; i++) {
                document.getElementById(`scene${i}`).style.display = i === 1 ? 'block' : 'none';
            }
            // 更新标题
            document.getElementById('sceneTitle').textContent = "Introduction to Newton's First Law";
            document.getElementById('sceneTitle').classList.add('active');
            // 更新场景编号
            document.getElementById('sceneNumber').textContent = '1';
        }
    },
    {
        title: "Understanding Inertia",
        subtitle: "Inertia is an object's resistance to changes in its state of motion. The greater the mass of an object, the greater its inertia and the more force it takes to change its motion.",
        action: () => {
            // 显示场景2，隐藏其他场景
            for (let i = 1; i <= 6; i++) {
                document.getElementById(`scene${i}`).style.display = i === 2 ? 'block' : 'none';
            }
            // 更新标题
            document.getElementById('sceneTitle').textContent = "Understanding Inertia";
            document.getElementById('sceneTitle').classList.add('active');
            // 更新场景编号
            document.getElementById('sceneNumber').textContent = '2';
        }
    },
    {
        title: "Objects at Rest",
        subtitle: "An object at rest will remain at rest unless acted upon by an unbalanced force. When multiple forces act on an object but cancel each other out, the object remains in equilibrium.",
        action: () => {
            // 显示场景3，隐藏其他场景
            for (let i = 1; i <= 6; i++) {
                document.getElementById(`scene${i}`).style.display = i === 3 ? 'block' : 'none';
            }
            // 更新标题
            document.getElementById('sceneTitle').textContent = "Objects at Rest";
            document.getElementById('sceneTitle').classList.add('active');
            // 更新场景编号
            document.getElementById('sceneNumber').textContent = '3';
        }
    },
    {
        title: "Objects in Motion",
        subtitle: "An object in motion will continue in motion with the same speed and in the same direction unless acted upon by an unbalanced force. This describes uniform motion in ideal frictionless conditions.",
        action: () => {
            // 显示场景4，隐藏其他场景
            for (let i = 1; i <= 6; i++) {
                document.getElementById(`scene${i}`).style.display = i === 4 ? 'block' : 'none';
            }
            // 更新标题
            document.getElementById('sceneTitle').textContent = "Objects in Motion";
            document.getElementById('sceneTitle').classList.add('active');
            // 更新场景编号
            document.getElementById('sceneNumber').textContent = '4';
        }
    },
    {
        title: "Unbalanced Forces and Motion Changes",
        subtitle: "When an unbalanced force acts on an object, it causes acceleration, changing the object's speed, direction, or both. This is the exception to Newton's First Law.",
        action: () => {
            // 显示场景5，隐藏其他场景
            for (let i = 1; i <= 6; i++) {
                document.getElementById(`scene${i}`).style.display = i === 5 ? 'block' : 'none';
            }
            // 更新标题
            document.getElementById('sceneTitle').textContent = "Unbalanced Forces and Motion Changes";
            document.getElementById('sceneTitle').classList.add('active');
            // 更新场景编号
            document.getElementById('sceneNumber').textContent = '5';
        }
    },
    {
        title: "Real-World Applications",
        subtitle: "Newton's First Law explains many everyday phenomena, from the need for seatbelts to why astronauts float in space. Understanding inertia helps us design safer and more efficient systems.",
        action: () => {
            // 显示场景6，隐藏其他场景
            for (let i = 1; i <= 6; i++) {
                document.getElementById(`scene${i}`).style.display = i === 6 ? 'block' : 'none';
            }
            // 更新标题
            document.getElementById('sceneTitle').textContent = "Real-World Applications";
            document.getElementById('sceneTitle').classList.add('active');
            // 更新场景编号
            document.getElementById('sceneNumber').textContent = '6';
        }
    }
];

// 全局变量
let currentScene = 0;
let isPlaying = false;
let isMuted = false;
let audioElements = [];
let sceneStartTime = 0;
let totalDuration = 0;
let sceneTimestamps = []; // 记录每个场景的开始时间戳
let isDragging = false;
let audioCache = {}; // 音频缓存对象
let cacheExpiry = 7 * 24 * 60 * 60 * 1000; // 缓存有效期：7天
let pausedScene = 0;
let pausedTime = 0;
let wasPausedByUser = false;

// DOM元素
const playBtn = document.getElementById('playBtn');
const playIcon = document.getElementById('playIcon');
const muteBtn = document.getElementById('muteBtn');
const muteIcon = document.getElementById('muteIcon');
const progressBar = document.getElementById('progressBar');
const progressFill = document.getElementById('progressFill');
const progressHandle = document.getElementById('progressHandle');
const subtitle = document.getElementById('subtitle');
const loading = document.getElementById('loading');
const audioContainer = document.getElementById('audioContainer');
const cacheIndicator = document.getElementById('cacheIndicator');
const timeDisplay = document.getElementById('timeDisplay');
const sceneTitle = document.getElementById('sceneTitle');
const sceneNumber = document.getElementById('sceneNumber');

// 初始化音频缓存
function initAudioCache() {
    try {
        // 尝试从localStorage加载缓存
        const cachedData = localStorage.getItem('ttsAudioCache');
        if (cachedData) {
            const parsedCache = JSON.parse(cachedData);
            
            // 检查缓存是否过期
            const now = Date.now();
            for (const key in parsedCache) {
                if (parsedCache[key].timestamp + cacheExpiry < now) {
                    delete parsedCache[key]; // 删除过期缓存
                }
            }
            
            audioCache = parsedCache;
            // 保存更新后的缓存
            localStorage.setItem('ttsAudioCache', JSON.stringify(audioCache));
        }
    } catch (e) {
        console.error('初始化音频缓存失败:', e);
        audioCache = {};
    }
}

// 将Blob转换为Base64
function blobToBase64(blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(blob);
    });
}

// 将Base64转换为Blob
function base64ToBlob(base64) {
    // 分离Base64数据和MIME类型
    const parts = base64.split(';base64,');
    const contentType = parts[0].split(':')[1];
    const raw = window.atob(parts[1]);
    
    // 转换为ArrayBuffer
    const uInt8Array = new Uint8Array(raw.length);
    for (let i = 0; i < raw.length; ++i) {
        uInt8Array[i] = raw.charCodeAt(i);
    }
    
    // 创建Blob
    return new Blob([uInt8Array], { type: contentType });
}

// 保存音频到缓存
async function saveAudioToCache(text, blob) {
    try {
        // 将Blob转换为Base64字符串
        const base64Data = await blobToBase64(blob);
        
        audioCache[text] = {
            data: base64Data,
            timestamp: Date.now()
        };
        localStorage.setItem('ttsAudioCache', JSON.stringify(audioCache));
    } catch (e) {
        console.error('保存音频缓存失败:', e);
    }
}

// 从缓存获取音频
function getAudioFromCache(text) {
    if (audioCache[text] && audioCache[text].timestamp + cacheExpiry > Date.now()) {
        try {
            // 将Base64转换回Blob
            const blob = base64ToBlob(audioCache[text].data);
            return URL.createObjectURL(blob);
        } catch (e) {
            console.error('从缓存恢复音频失败:', e);
            return null;
        }
    }
    return null;
}

// 初始化音频元素
function initializeAudioElements() {
    // 清空容器
    audioContainer.innerHTML = '';
    audioElements = [];
    
    // 为每个场景创建音频元素
    scenes.forEach((scene, index) => {
        const audio = document.createElement('audio');
        audio.id = `audio-${index}`;
        audio.preload = 'auto';
        audioContainer.appendChild(audio);
        audioElements.push(audio);
    });
}

// 合成单个场景的语音
async function synthesizeSceneSpeech(index) {
    const text = scenes[index].subtitle;
    const encodedText = encodeURIComponent(text);
    
    // 检查缓存
    const cachedAudioUrl = getAudioFromCache(text);
    if (cachedAudioUrl) {
        // 显示缓存指示器
        cacheIndicator.classList.add('active');
        setTimeout(() => {
            cacheIndicator.classList.remove('active');
        }, 2000);
        
        // 使用缓存的音频
        audioElements[index].src = cachedAudioUrl;
        
        // 等待音频加载完成以获取时长
        return new Promise((resolve) => {
            audioElements[index].onloadedmetadata = () => {
                scenes[index].duration = audioElements[index].duration * 1000; // 转换为毫秒
                resolve();
            };
        });
    }
    
    // 缓存中没有，请求TTS服务
    try {
        const response = await fetch(`https://javalinux.explanation.fun/tts?input=${encodedText}`);
        const blob = await response.blob();
        const audioUrl = URL.createObjectURL(blob);
        
        // 保存到缓存
        await saveAudioToCache(text, blob);
        
        audioElements[index].src = audioUrl;
        
        // 等待音频加载完成以获取时长
        return new Promise((resolve) => {
            audioElements[index].onloadedmetadata = () => {
                scenes[index].duration = audioElements[index].duration * 1000; // 转换为毫秒
                resolve();
            };
        });
    } catch (error) {
        console.error(`场景 ${index} TTS合成失败:`, error);
        // 使用默认持续时间
        scenes[index].duration = 5000;
        resolve();
    }
}

// 格式化时间显示
function formatTime(milliseconds) {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

// 更新时间显示
function updateTimeDisplay(currentTime, totalTime) {
    const currentTimeStr = formatTime(currentTime);
    const totalTimeStr = formatTime(totalTime);
    timeDisplay.textContent = `${currentTimeStr} / ${totalTimeStr}`;
}

// 预加载所有音频
async function preloadAllAudio() {
    loading.classList.add('active');
    playBtn.disabled = true;
    
    // 初始化音频缓存
    initAudioCache();
    
    initializeAudioElements();
    
    // 顺序合成每个场景的语音
    for (let i = 0; i < scenes.length; i++) {
        await synthesizeSceneSpeech(i);
    }
    
    // 计算时间戳
    calculateTimestamps();
    
    // 初始化时间显示
    updateTimeDisplay(0, totalDuration);
    
    loading.classList.remove('active');
    playBtn.disabled = false;
}

// 计算每个场景的时间戳
function calculateTimestamps() {
    sceneTimestamps = [];
    let accumulatedTime = 0;
    
    scenes.forEach((scene) => {
        sceneTimestamps.push(accumulatedTime);
        accumulatedTime += scene.duration;
    });
    
    totalDuration = accumulatedTime;
}

// 更新进度条
function updateProgress() {
    if (!isPlaying) return;
    
    const currentAudio = audioElements[currentScene];
    if (!currentAudio) return;
    
    // 计算当前时间点
    const sceneStartTime = sceneTimestamps[currentScene];
    const currentTime = sceneStartTime + (currentAudio.currentTime * 1000);
    const progress = Math.min((currentTime / totalDuration) * 100, 100);
    
    progressFill.style.width = progress + '%';
    progressHandle.style.left = progress + '%';
    
    // 更新时间显示
    updateTimeDisplay(currentTime, totalDuration);
    
    if (progress >= 100) {
        stopAnimation();
    } else {
        requestAnimationFrame(updateProgress);
    }
}

// 播放指定场景
function playScene(sceneIndex) {
    if (sceneIndex >= scenes.length) {
        stopAnimation();
        return;
    }
    
    // 停止当前音频
    if (currentScene < audioElements.length && audioElements[currentScene]) {
        audioElements[currentScene].pause();
    }
    
    currentScene = sceneIndex;
    const scene = scenes[sceneIndex];
    const audio = audioElements[sceneIndex];
    
    // 更新字幕和动画
    subtitle.textContent = scene.subtitle;
    subtitle.classList.add('active');
    scene.action();
    
    // 播放音频
    if (audio && !isMuted) {
        audio.currentTime = 0;
        audio.play().catch(e => console.log('音频播放失败:', e));
        
        // 音频结束时播放下一个场景
        audio.onended = () => {
            subtitle.classList.remove('active');
            playScene(currentScene + 1);
        };
    } else {
        // 如果静音或没有音频，使用定时器
        setTimeout(() => {
            subtitle.classList.remove('active');
            playScene(currentScene + 1);
        }, scene.duration);
    }
}

// 开始动画
function startAnimation() {
    if (isPlaying) return;
    
    isPlaying = true;
    playIcon.textContent = '⏸';
    
    if (wasPausedByUser && pausedScene < scenes.length) {
        // 从暂停的状态恢复播放
        const scene = scenes[pausedScene];
        const audio = audioElements[pausedScene];
        
        // 更新字幕和动画
        subtitle.textContent = scene.subtitle;
        subtitle.classList.add('active');
        scene.action();
        
        // 设置当前场景
        currentScene = pausedScene;
        
        // 播放音频（从暂停的时间点开始）
        if (audio && !isMuted) {
            audio.currentTime = pausedTime / 1000; // 转换为秒
            audio.play().catch(e => console.log('音频播放失败:', e));
            
            // 音频结束时播放下一个场景
            audio.onended = () => {
                subtitle.classList.remove('active');
                playScene(currentScene + 1);
            };
        } else {
            // 如果静音或没有音频，使用定时器
            setTimeout(() => {
                subtitle.classList.remove('active');
                playScene(currentScene + 1);
            }, scene.duration - pausedTime);
        }
        
        wasPausedByUser = false;
    } else {
        // 从头开始播放
        currentScene = 0;
        playScene(0);
    }
    
    updateProgress();
}

// 暂停动画
function pauseAnimation() {
    isPlaying = false;
    playIcon.textContent = '▶';
    wasPausedByUser = true;
    
    // 保存当前播放状态
    if (currentScene < audioElements.length && audioElements[currentScene]) {
        const audio = audioElements[currentScene];
        pausedScene = currentScene;
        pausedTime = audio.currentTime * 1000; // 转换为毫秒
        
        // 暂停当前音频
        audio.pause();
    }
}

// 停止动画
function stopAnimation() {
    pauseAnimation();
    currentScene = 0;
    pausedScene = 0;
    pausedTime = 0;
    wasPausedByUser = false;
    progressFill.style.width = '0%';
    progressHandle.style.left = '0%';
    subtitle.textContent = '';
    
    // 重置时间显示
    updateTimeDisplay(0, totalDuration);
    
    // 隐藏所有场景
    for (let i = 1; i <= 6; i++) {
        document.getElementById(`scene${i}`).style.display = 'none';
    }
    
    // 隐藏标题
    sceneTitle.classList.remove('active');
    // 重置场景编号
    sceneNumber.textContent = '1';
}

// 切换静音
function toggleMute() {
    isMuted = !isMuted;
    muteIcon.textContent = isMuted ? '🔇' : '🔊';
    
    // 设置所有音频元素的静音状态
    audioElements.forEach(audio => {
        audio.muted = isMuted;
    });
}

// 进度条控制
function handleProgressClick(e) {
    const rect = progressBar.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const percentage = Math.max(0, Math.min(100, (x / rect.width) * 100));
    
    progressFill.style.width = percentage + '%';
    progressHandle.style.left = percentage + '%';
    
    // 计算对应的时间点
    const targetTime = (percentage / 100) * totalDuration;
    
    // 更新时间显示
    updateTimeDisplay(targetTime, totalDuration);
    
    // 找到对应的场景
    let targetScene = 0;
    for (let i = 0; i < sceneTimestamps.length; i++) {
        if (targetTime >= sceneTimestamps[i]) {
            targetScene = i;
        } else {
            break;
        }
    }
    
    // 更新暂停状态
    pausedScene = targetScene;
    pausedTime = targetTime - sceneTimestamps[targetScene];
    
    // 如果正在播放，跳转到目标场景
    if (isPlaying) {
        // 停止当前音频
        if (currentScene < audioElements.length && audioElements[currentScene]) {
            audioElements[currentScene].pause();
        }
        
        // 计算在目标场景中的时间偏移
        const sceneOffset = targetTime - sceneTimestamps[targetScene];
        
        // 播放目标场景
        currentScene = targetScene;
        const scene = scenes[targetScene];
        const audio = audioElements[targetScene];
        
        // 更新字幕和动画
        subtitle.textContent = scene.subtitle;
        subtitle.classList.add('active');
        scene.action();
        
        // 播放音频（从指定时间点开始）
        if (audio && !isMuted) {
            audio.currentTime = sceneOffset / 1000; // 转换为秒
            audio.play().catch(e => console.log('音频播放失败:', e));
            
            // 音频结束时播放下一个场景
            audio.onended = () => {
                subtitle.classList.remove('active');
                playScene(currentScene + 1);
            };
        } else {
            // 如果静音或没有音频，使用定时器
            setTimeout(() => {
                subtitle.classList.remove('active');
                playScene(currentScene + 1);
            }, scene.duration - sceneOffset);
        }
    } else if (wasPausedByUser) {
        // 如果是暂停状态，更新场景显示但不播放
        currentScene = targetScene;
        const scene = scenes[targetScene];
        
        // 更新字幕和动画
        subtitle.textContent = scene.subtitle;
        subtitle.classList.add('active');
        scene.action();
    }
}

// 事件监听
playBtn.addEventListener('click', () => {
    if (isPlaying) {
        pauseAnimation();
    } else {
        startAnimation();
    }
});

muteBtn.addEventListener('click', toggleMute);
progressBar.addEventListener('click', handleProgressClick);

// 进度条拖动
progressHandle.addEventListener('mousedown', () => isDragging = true);
document.addEventListener('mousemove', (e) => {
    if (isDragging) {
        handleProgressClick(e);
    }
});
document.addEventListener('mouseup', () => {
    if (isDragging) {
        isDragging = false;
    }
});

// 初始化
window.addEventListener('load', () => {
    preloadAllAudio();
});