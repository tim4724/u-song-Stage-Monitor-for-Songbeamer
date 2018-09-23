<section id='timeBox'
         class='currentSection currentPage'
         style='font-size: 4em;
                border-color: #00a39d;
                margin-top: 64px;
                opacity: 0;
                transition: opacity 1s linear'>
    <span id="timeHoursMinutes"></span><span id='timeSeconds'
                                             style="font-size: 0.7em;
                                             margin-left: 0.1em;
                                             color: #C1C1C1;"></span>
</section>

<script>
    setTimeout(function () {
        document.getElementById('timeBox').style.opacity = '1';
    }, 2000);

    const timeHoursMinutes = document.getElementById('timeHoursMinutes');
    const timeSeconds = document.getElementById('timeSeconds');

    const updateTime = function () {
        const date = new Date();
        var hours = date.getHours();
        var minutes = date.getMinutes();
        var seconds = date.getSeconds();
        if (minutes < 10) {
            minutes = '0' + minutes;
        }
        if (seconds < 10) {
            seconds = '0' + seconds;
        }
        timeHoursMinutes.innerText = hours + ':' + minutes;
        timeSeconds.innerText = seconds;
    };
    setInterval(updateTime, 1000);
</script>
