<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.SettingsView" -->
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=0.15, maximum-scale=0.15, user-scalable=0"/>
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/settings.css">
    <title>${messages.getString("settings")}</title>
    <script>
        function simplePutRequest(path, forceReload = false) {
            let xhr = new XMLHttpRequest();
            xhr.open('PUT', path, true);
            xhr.onloadend = function () {
                if (xhr.status !== 200) {
                    alert(xhr.responseText);
                    location.reload(true);
                } else if (forceReload) {
                    location.reload(true);
                }
            };
            xhr.send()
        }

        function onCheckedChanged(id) {
            const cb = document.getElementById(id);
            simplePutRequest("settings?" + id + "=" + cb.checked);
        }

        function onInputChanged(id, forceReload = false) {
            const input = document.getElementById(id);
            simplePutRequest("settings?" + id + "=" + input.value, forceReload);
        }
    </script>
</head>

<body>
<main>
    <h1 id="title">${messages.getString("settings")}</h1>

    <section class="settingGroup">
        <div class="settingGroupTitle">${messages.getString("general")}</div>
        <div class="setting">
            <span class="settingText">${messages.getString("autostart")}</span>
            <label class="switch right">
                <input id="autoStart" type="checkbox" ${isAutostartEnabled()?then("checked", "")}
                       onchange="new function () {onCheckedChanged('autoStart')}">
                <span class="slider"></span>
            </label>
        </div>

        <div class="setting">
            <span class="settingText">${messages.getString("showSplash")}</span>
            <label class="switch right">
                <input id="splashScreen" type="checkbox" ${isShowSplash()?then("checked", "")}
                       onchange="new function () {onCheckedChanged('splashScreen')}">
                <span class="slider"></span>
            </label>
        </div>
        
        <div class="setting">
            <span class="settingText">${messages.getString("showClockInSong")}</span>
            <label class="switch right">
                <input id="showClockInSong" type="checkbox"  ${isShowClockInSong()?then("checked", "")}
                       onchange="new function() {onCheckedChanged('showClockInSong')};">
                <span class="slider"></span>
            </label>
        </div>

        <div class="setting">
            <span class="settingText">${messages.getString("checkUpdates")}</span>
            <label class="switch right">
                <input id="checkUpdates" type="checkbox"  ${isNotifyUpdates()?then("checked", "")}
                       onchange="new function() {onCheckedChanged('checkUpdates')};">
                <span class="slider"></span>
            </label>
        </div>

        <#if showNotifyUpdatesSongbeamer()>
            <div class="setting">
                <span class="settingText">${messages.getString("checkUpdatesSongbeamer")}</span>
                <label class="switch right">
                    <input id="checkSongbeamerUpdates"
                           type="checkbox"  ${isNotifySongbeamerUpdates()?then("checked", "")}
                           onchange="new function() {onCheckedChanged('checkSongbeamerUpdates')};">
                    <span class="slider"></span>
                </label>
            </div>
        </#if>
    </section>

    <section class="settingGroup">
        <div class="settingGroupTitle">${messages.getString("songbeamer")}</div>
        <div class="setting">
            <span class="settingText">${messages.getString("songDir")}</span>
            <span class="right">
                <#if isAllowSetSongDir()>
                    ${getSongDir()}
                    <button id="songDir" value="true" onclick="new function() {onInputChanged('songDir', true)};">
                        ${messages.getString("select")}
                    </button>
                <#else>
                    <span class="disabled">${getSongDir()}</span>
                </#if>
            </span>
        </div>

        <div class="setting">
            <span class="settingText">${messages.getString("titleHasOwnPage")}</span>
            <label class="switch right ${isAllowSetTitleHasOwnPage()?then("", "disabled")}">
                <input id="titleHasPage" ${isTitleOwnPage()?then("checked", "")}
                       type="checkbox" ${isAllowSetTitleHasOwnPage()?then("", "disabled")}
                       onchange="new function() {onCheckedChanged('titleHasPage')};">
                <span class="slider"></span>
            </label>
        </div>

        <div class="setting">
            <span class="settingText">${messages.getString("maxLinesPerPage")}</span>
            <label class="right">
                <#if isAllowSetMaxLinesPerPage()>
                    <input id="maxLinesPage" type="number" name="maxLinesPage" min="0" max="10"
                           value="${getMaxLinesPage()}"
                           onchange="new function() {onInputChanged('maxLinesPage')};">
                <#else>
                    <span class="disabled">
                        ${(getMaxLinesPage() == 0)?then("-", "" + getMaxLinesPage())}
                    </span>
                </#if>
            </label>
        </div>

        <div class="warning">${messages.getString("restartSongbeamerWarning")}</div>
    </section>
</main>
</body>
</html>
