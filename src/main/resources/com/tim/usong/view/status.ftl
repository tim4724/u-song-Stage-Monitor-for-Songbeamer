<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.StatusView" -->
<html>
<head>
    <meta name="viewport" content="width=device-width,initial-scale=0.15"/>
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/status.css">
    <script>
        const connectToWebSocket = () => {
            const errorElement = document.getElementById("errorBox");
            const ws = new WebSocket("ws://" + location.host + "/song/ws?status=true");
            ws.onopen = () => {
                errorElement.style.display = "none";
            };
            ws.onmessage = (ev) => {
                location.reload(true);
            };
            ws.onclose = ev => {
                setTimeout(connectToWebSocket, 500);
                console.error("ws closed" + ev.reason);
                errorElement.style.display = "block";
            };
        }
    </script>
</head>
<body onload="connectToWebSocket()">

<h1 id="title">${messages.getString("status")}</h1>

<section>
    <table>
        <tr>
            <th>${messages.getString("version")}</th>
            <td>
                <#if status.version??>
                    ${status.version}
                <#else>
                    ${messages.getString("unknown")}
                </#if>
            </td>
        </tr>
        <tr>
            <th>${messages.getString("autostart")}</th>
            <td>
                <#if status.startWithWindows>
                    ${messages.getString("yes")}
                <#else>
                    ${messages.getString("no")}
                </#if>
            </td>
        </tr>
        <tr>
            <th>${messages.getString("activeClientsCount")}</th>
            <td>
                <#if status.clientCount == 0>
                    <span class="negative">#{status.clientCount}</span>
                <#else>
                    ${status.clientCount}
                </#if>
            </td>
        </tr>
        <tr>
            <th>${messages.getString("hostname")}</th>
            <td>${status.hostname}</td>
        </tr>
        <tr>
            <th>${messages.getString("ipAddress")}</th>
            <td>${status.ipAddress}</td>
        </tr>
    </table>

    <table>
        <tr>
            <th>${messages.getString("songbeamerSender")}</th>
            <td>
                <#if !status.connected>
                    <span class="negative">${messages.getString("notConnected")}</span>
                <#else >
                    <span class="positive">${messages.getString("connected")}</span>
                </#if>
            </td>
        </tr>
        <tr>
            <th>${messages.getString("songbeamerVersion")}</th>
            <td>${status.sbVersion}</td>
        </tr>
        <tr>
            <th>${messages.getString("songDir")}</th>
            <td>
                <#if status.songCount == 0>
                    <span class="negative"> ${status.songDir} </span>
                <#else>
                    <span> ${status.songDir} </span>
                </#if>
            </td>
        </tr>
        <tr>
            <th>${messages.getString("songCount")}</th>
            <td>
                <#if status.songCount == 0>
                    <span class="negative"> ${status.songCount} </span>
                <#else>
                    <span> ${status.songCount} </span>
                </#if>
            </td>
        </tr>
    </table>

    <table>
        <tr>
            <th>${messages.getString("currentSong")}</th>
            <td>${status.songTitle}</td>
        </tr>
        <#if status.currentSection?? >
            <tr>
                <th>${messages.getString("currentSection")}</th>
                <td>${status.currentSection}</td>
            </tr>
        </#if>
        <tr>
            <th>${messages.getString("currentPage")}</th>
            <td><#if status.currentPage == -1>-<#else>${status.currentPage +1}</#if></td>
        </tr>
        <#if (status.langCount > 1)>
            <tr>
                <th>${messages.getString("currentLang")}</th>
                <td>${status.lang} / ${status.langCount}</td>
            </tr>
        </#if>
    </table>
</section>

<div id="errorBox">
    &#9888; ${messages.getString("connectionLost")}
</div>

</body>
</html>