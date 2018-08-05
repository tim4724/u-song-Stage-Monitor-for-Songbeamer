<section id="timeBox"
         class="currentSection currentPage"
         style="font-size: 4em;
                border-color: #00a39d;
                margin-top: 32px;">
    &nbsp;
</section>

<script>
    const timeBox = document.getElementById("timeBox");
    const updateTimeBox = () => {
        timeBox.innerHTML = (new Date()).toLocaleTimeString(navigator.language).replace("MESZ", "");
    };
    updateTimeBox();
    setInterval(updateTimeBox, 1000);
</script>
