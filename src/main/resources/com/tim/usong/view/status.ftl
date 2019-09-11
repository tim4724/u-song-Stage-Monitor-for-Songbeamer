<#ftl encoding='UTF-8'>
<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.StatusView" -->
<html>
<head>
    <meta name="viewport" content="width=device-width,initial-scale=0.15"/>
    <meta charset="UTF-8">
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/status.css">
    <title>${messages.getString("status")}</title>
    <script>
        const connectToWebSocket = function () {
            const errorElement = document.getElementById("errorBox");
            const ws = new WebSocket("ws://" + location.host + "/song/ws?status=true");
            ws.onopen = function () {
                errorElement.style.display = "none";
                window.onbeforeunload = function () {
                    ws.onclose = function () {
                    };
                    ws.close();
                };
            };
            ws.onmessage = function () {
                location.reload(true);
            };
            ws.onclose = function (ev) {
                setTimeout(connectToWebSocket, 500);
                console.error("ws closed" + ev.reason);
                errorElement.style.display = "block";
            };
        }
    </script>
</head>
<body onload="connectToWebSocket()">

<main>
    <h1 unselectable="on" id="title">${messages.getString("status")}</h1>

    <table>
        <tr>
            <th unselectable="on">${messages.getString("version")}</th>
            <td unselectable="on">
                <#if status.version??>
                    ${status.version}
                <#else>
                    ${messages.getString("unknown")}
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("autostart")}</th>
            <td unselectable="on">
                <#if status.startWithWindows>
                    ${messages.getString("yes")}
                <#else>
                    ${messages.getString("no")}
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("activeClientsCount")}</th>
            <td unselectable="on">
                <#if status.clientCount == 0 || (status.clientCount == 1 && status.preview)>
                    <span class="negative">#{status.clientCount}</span>
                <#else>
                    ${status.clientCount}
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("hostname")}</th>
            <td unselectable="on">${status.hostname}</td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("ipAddress")}</th>
            <td unselectable="on">${status.ipAddress}</td>
        </tr>
    </table>

    <table>
        <tr>
            <th unselectable="on">${messages.getString("songbeamerVersion")}</th>
            <td unselectable="on">${status.sbVersion}</td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("songbeamerSender")}</th>
            <td>
                <#if !status.connected>
                    <span unselectable="on" class="negative">${messages.getString("notConnected")}</span>
                <#else >
                    <span unselectable="on" class="positive">${messages.getString("connected")}</span>
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("songDir")}</th>
            <td unselectable="on">
                <#if status.songCount == 0>
                    <span unselectable="on" class="negative"> ${status.songDir} </span>
                <#else>
                    ${status.songDir}
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("songCount")}</th>
            <td unselectable="on">
                <#if status.songCount == 0>
                    <span unselectable="on" class="negative"> ${status.songCount} </span>
                <#else>
                    ${status.songCount}
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("titleHasOwnPage")}</th>
            <td unselectable="on">
                <#if status.titelHasOwnPage>
                    ${messages.getString("yes")}
                <#else>
                    ${messages.getString("no")}
                </#if>
            </td>
        </tr>
        <tr>
            <th unselectable="on">${messages.getString("maxLinesPerPage")}</th>
            <#if (status.maxLinesPerPage > 0) >
                <td unselectable="on">${status.maxLinesPerPage}</td>
            <#else>
                <td unselectable="on">${messages.getString("unlimited")}</td>
            </#if>
        </tr>
    </table>

    <table>
        <#if (status.fullscreenDisplay >= 0) >
            <tr>
                <th unselectable="on">${messages.getString("fullscreenMode")}</th>
                <td unselectable="on">${messages.getString("display")} ${status.fullscreenDisplay +1}</td>
            </tr>
        </#if>
        <tr>
            <th unselectable="on">${messages.getString("currentSong")}</th>
            <td unselectable="on">${status.songTitle}</td>
        </tr>
        <#if status.currentSection?? >
            <tr>
                <th unselectable="on">${messages.getString("currentSection")}</th>
                <td unselectable="on">${status.currentSection}</td>
            </tr>
        </#if>
        <tr>
            <th unselectable="on">${messages.getString("currentPage")}</th>
            <td unselectable="on"><#if status.currentPage == -1>-<#else>${status.currentPage +1}</#if></td>
        </tr>
        <#if (status.langCount > 1)>
            <tr>
                <th unselectable="on">${messages.getString("currentLang")}</th>
                <td unselectable="on">${status.lang} / ${status.langCount}</td>
            </tr>
        </#if>
        <tr>
            <th unselectable="on">${messages.getString("songHasChords")}</th>
            <td unselectable="on">
                <#if status.songHasChords>
                    ${messages.getString("yes")}
                <#else>
                    ${messages.getString("no")}
                </#if>
            </td>
        </tr>
    </table>
</main>

<div id="errorBox" unselectable="on">
    &#9888; ${messages.getString("connectionLost")}
</div>

</body>
</html>
