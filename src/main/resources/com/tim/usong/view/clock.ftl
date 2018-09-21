<section id='timeBox'
         class='currentSection currentPage'
         style='font-size: 4em;
                border-color: #00a39d;
                margin-top: 64px;
                animation-name: unset;
                opacity: 0;
                transition: opacity 1s linear'>
    <span id="timeHoursMinutes">&nbsp;</span>
    <span id='timeSeconds' style="font-size: 0.5em; color: #C1C1C1;"></span>
</section>

<script>
    setTimeout(function () {
        document.getElementById('timeBox').style.opacity = '1';
    }, 2000);

    const locale = '${.locale?replace("_", "-")}';
    const timeHoursMinutes = document.getElementById('timeHoursMinutes');
    const timeSeconds = document.getElementById('timeSeconds');

    const updateTimeBox = function () {
        const time = (new Date()).toLocaleTimeString(locale);
        const secondsIndex = time.lastIndexOf(':');
        timeHoursMinutes.innerHTML = time.substr(0, secondsIndex);
        timeSeconds.innerHTML = ' ' + time.substr(secondsIndex + 1, secondsIndex + 3);
    };
    updateTimeBox();
    setInterval(updateTimeBox, 1000);
</script>
