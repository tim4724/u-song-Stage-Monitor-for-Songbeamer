<#ftl encoding='UTF-8'>
<section id='timeBox'
         class='currentSection currentPage'
         style='border-color: #00a39d;
                opacity: 0;
                transition: opacity 2s linear'>
    <span style='font-size: 4em;'>
    <span unselectable="on" id="timeHoursMinutes"></span><span unselectable="on" id='timeSeconds' style="font-size: 0.7em;
                                             margin-left: 0.1em;
                                             color: #C1C1C1;"></span>
    </span>
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
        if (hours < 10) {
            hours = '0' + hours;
        }
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
</script>
