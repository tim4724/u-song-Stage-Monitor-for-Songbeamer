<section id='timeBox'
         class='currentSection currentPage'
         style='font-size: 4em;
                border-color: #00a39d;
                margin-top: 64px;
                animation-name: unset;
                opacity: 0;
                transition: opacity 1s linear'>
    &nbsp;
</section>

<script>
    const timeBox = document.getElementById('timeBox');
    setTimeout(function () {
        timeBox.style.opacity = '1';
    }, 2000);
    const updateTimeBox = function () {
        timeBox.innerHTML = (new Date()).toLocaleTimeString(navigator.language).replace('MESZ', '');
    };
    updateTimeBox();
    setInterval(updateTimeBox, 1000);
</script>
