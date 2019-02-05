<section id='timeBox'
         class='currentSection currentPage'
         style='font-size: 4em;
                border-color: #00a39d;
                margin-top: 64px;
                opacity: 0;
                transition: opacity 2s linear'>
    <span id="timeHoursMinutes"></span><span id='timeSeconds'
                                             style="font-size: 0.7em;
                                             margin-left: 0.1em;
                                             color: #C1C1C1;"></span>

    <title>${messages.getString("clock")}</title>
</section>

<script>
    const timeHoursMinutes = document.getElementById('timeHoursMinutes');
    const timeSeconds = document.getElementById('timeSeconds');

    setTimeout(function () {
        document.getElementById('timeBox').style.opacity = "1";
    }, 500);
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
    updateTime();
    const updateClockInterval = setInterval(updateTime, 1000);
    const clockStopUpdating = function () {
        clearInterval(updateClockInterval);
    }
</script>
