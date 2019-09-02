<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.SettingsView" -->
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=0.15, maximum-scale=0.15, user-scalable=0"/>
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/settings.css">
    <title>${messages.getString("settings")}</title>
    <script src="/assets/js/settings.js"></script>
</head>

<body onload="connectToWebSocket()">
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
            <span class="settingText">${messages.getString("checkUpdates")}</span>
            <label class="switch right">
                <input id="checkUpdates" type="checkbox" ${isNotifyUpdates()?then("checked", "")}
                       onchange="new function() {onCheckedChanged('checkUpdates')};">
                <span class="slider"></span>
            </label>
        </div>

        <#if showNotifyUpdatesSongbeamer()>
            <div class="setting">
                <span class="settingText">${messages.getString("checkUpdatesSongbeamer")}</span>
                <label class="switch right">
                    <input id="checkSongbeamerUpdates"
                           type="checkbox" ${isNotifySongbeamerUpdates()?then("checked", "")}
                           onchange="new function() {onCheckedChanged('checkSongbeamerUpdates')};">
                    <span class="slider"></span>
                </label>
            </div>
        </#if>
    </section>

    <section class="settingGroup">
        <div class="settingGroupTitle">${messages.getString("presentation")}</div>

        <div class="setting">
            <span class="settingText">${messages.getString("showFullscreen")}</span>
            <label class="right">
                <#if (getScreensCount() > 1 || true)>
                    <select id="showOnDisplay" onchange="new function() {onInputChanged('showOnDisplay')};">
                        <option value="-1" ${(getFullscreenDisplay() == -1)?then("selected", "")}>
                            ${messages.getString("showNotFullscreen")}
                        </option>
                        <#list 1..screensCount as i>
                            <option value="${i-1}" ${(getFullscreenDisplay() == i-1)?then("selected", "")}>
                                ${messages.getString("display")} ${i}
                            </option>
                        </#list>
                    </select>
                <#else>
                    <span class="disabled">
                        ${messages.getString("showFullscreenNotAvailable")}
                    </span>
                </#if>
            </label>
        </div>

        <div class="setting">
            <span class="settingText">${messages.getString("showClockInSong")}</span>
            <label class="switch right">
                <input id="showClockInSong" type="checkbox" ${isShowClockInSong()?then("checked", "")}
                       onchange="new function() {onCheckedChanged('showClockInSong')};">
                <span class="slider"></span>
            </label>
        </div>

        <div class="setting">
            <span class="settingText">${messages.getString("showChords")}</span>
            <label class="switch right">
                <input id="chords" type="checkbox" ${isShowChords()?then("checked", "")}
                       onchange="new function() {onCheckedChanged('chords')};">
                <span class="slider"></span>
            </label>
        </div>
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

        <#noautoesc>
            <div class="warning">${messages.getString("restartSongbeamerWarning")}</div>
        </#noautoesc>
    </section>

    <div id="savedHint" class="invisible">
        ${messages.getString("saved")} &#x1f4be;
    </div>


</main>

<script>
    window.onerror = function () {
        alert("Error")
    };
</script>

</body>
</html>
