// 场景显示函数
function showScene(sceneNumber, title) {
    // 隐藏所有场景
    for (let i = 1; i <= mathScenes.length; i++) {
        const scene = document.getElementById(`scene${i}`);
        if (scene) {
            scene.style.display = 'none';
        }
    }

    // 显示当前场景
    const currentScene = document.getElementById(`scene${sceneNumber}`);
    if (currentScene) {
        currentScene.style.display = 'flex';
    }

    // 更新标题和场景编号
    const sceneTitle = document.getElementById('sceneTitle');
    const sceneNumberElement = document.getElementById('sceneNumber');

    if (sceneTitle) {
        sceneTitle.textContent = title;
        sceneTitle.classList.add('active');
    }

    if (sceneNumberElement) {
        sceneNumberElement.textContent = sceneNumber.toString();
    }
}


// 初始化动画播放器
async function initializePlayer() {
    // 获取DOM元素
    const elements = {
        audioContainer: document.getElementById('audioContainer'),
        loading: document.getElementById('loading'),
        playBtn: document.getElementById('playBtn'),
        playIcon: document.getElementById('playIcon'),
        muteBtn: document.getElementById('muteBtn'),
        muteIcon: document.getElementById('muteIcon'),
        progressBar: document.getElementById('progressBar'),
        progressFill: document.getElementById('progressFill'),
        progressHandle: document.getElementById('progressHandle'),
        subtitle: document.getElementById('subtitle'),
        timeDisplay: document.getElementById('timeDisplay'),
        cacheIndicator: document.getElementById('cacheIndicator')
    };

    const player = new AnimationUtils.AnimationPlayer({
        scenes: mathScenes,
        elements: elements,
    });

    // 初始化播放器
    await player.init();

    // 预加载音频
    // 显示 loading 层
    elements.loading?.classList.add('active');


    player.setSceneButtons(document.getElementById('sceneButtons'));

    await player.preloadAllAudio((current, total) => {
        // 百分比（防止 total 为 0）
        const percent = total ? Math.round((current / total) * 100) : 0;

        // DOM 引用
        const fillEl = document.getElementById('loadingProgressFill');
        const textEl = document.getElementById('loadingProgressText');
        const loadingEl = document.getElementById('loading');

        // 进度条宽度与文本
        if (fillEl) fillEl.style.width = percent + '%';
        if (textEl) textEl.textContent = `Progress: ${current} / ${total}（${percent}%）`;

        // 无障碍属性
        if (loadingEl) loadingEl.setAttribute('aria-valuenow', String(percent));
    });

    // 预加载完成后：隐藏 loading，启用播放按钮
    elements.loading?.classList.remove('active');
    elements.playBtn.disabled = false;

    console.log('动画播放器初始化完成');
    await player.playWithErrorHandling()

    window.mathPlayer = player;
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', initializePlayer);

// 错误处理
window.addEventListener('error', (e) => {
    console.error('页面错误:', e);
    const loading = document.getElementById('loading');
    if (loading && loading.classList.contains('active')) {
        loading.innerHTML = `
            <div style="color: #e74c3c; text-align: center;">
                <div style="font-size: 2rem;">⚠️</div>
                <div>加载失败，请刷新页面重试</div>
            </div>
        `;
    }
});