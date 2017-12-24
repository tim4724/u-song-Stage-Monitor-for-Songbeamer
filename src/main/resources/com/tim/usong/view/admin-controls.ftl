<#-- @ftlvariable name="" type="com.tim.usong.view.SongView" -->

<script>
    function changeLang(newLang) {
        let xhr = new XMLHttpRequest();
        xhr.open('POST', 'song/lang/' + newLang, true);
        xhr.send(null);
    }

    function changePage(newPage) {
        let xhr = new XMLHttpRequest();
        xhr.open('POST', 'song/page/' + newPage, true);
        xhr.send(null);
    }

    function updateActiveClientCount() {
        let xhr = new XMLHttpRequest();
        xhr.open('GET', 'song/activeClients', true);
        xhr.onload = function () {
            try {
                if (xhr.status === 200) {
                    document.getElementById('activeClients').innerHTML = xhr.responseText;
                }
            } catch (e) {
                console.error(e);
            } finally {
                updateActiveClientCount();
            }
        };
        xhr.send(null);
    }
</script>

<footer id="controlsWrapper">
    <span id="activeClients">-</span>
    <button type="button" id="upButton" onclick="changePage(window.lastPageNumber - 1)">&#9650;</button>
    <button type="button" id="downButton" onclick="changePage(window.lastPageNumber + 1)">&#9660;</button>

<#if (song.langCount > 1)>
    <#list 1..song.langCount as i>
        <button type="button" <#if i == song.lang> class="active" disabled</#if>
                onclick="changeLang(${i});">Sprache ${i}</button>
    </#list>
</#if>
</footer>

<script>
    updateActiveClientCount();
</script>