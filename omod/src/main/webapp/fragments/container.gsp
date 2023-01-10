<%
    ui.includeJavascript("cdrsync", "jquery.js")
    ui.includeCss("cdrsync", "style.css")

%>
<%= ui.resourceLinks() %>
<div id="overlay">
    <div class="cv-spinner">
        <span class="spinner"></span>
    </div>
</div>
<div class="container-wrap">
    <div class="flex-container">
        <div>
            <button id="initial" style="color: red"><b>Sync From Initial</b></button>
        </div>
        <div>
            <button id="update" style="color: green"><b>Sync Update</b></button>
        </div>
        <div>
            <button id="custom" style="color: blue"><b>Custom Sync</b></button>
        </div>
    </div>
    <br/>
    <br/>
    <div class="input-container" id="custom_date">
        <div>
            <label for="start"><b>Start Date</b></label>
            <br/>
            <input type="date" id="start" name="startDate"/>
        </div>

        <div>
            <label for="end"><b>End Date</b></label>
            <br/>
            <input type="date" id="end" name="endDate"/>
        </div>
        <br/>
        <div>
            <button id="custom_sync" style="color: blue"><em><b>Sync</b></em></button>
        </div>
    </div>
</div>


<script type="text/javascript">
    var jq = jQuery;
    jq("#custom_date").hide();

    jq(document).ajaxSend(function() {
        jq("#overlay").fadeIn(300);
    }).ajaxComplete(function (){
        setTimeout(function(){
            jq("#overlay").fadeOut(300);
        },500);
    });

    jq("#initial").click(function(){
        alert("Syncing from inception");
        syncInitial().then(data => {
            if(data === "Syncing successful") {
                saveSyncDate();
            }
            alert(data);
        }, error => alert(error.message));
    });

    jq("#update").click(function(){
        alert("Syncing from last sync date");
        syncUpdate().then(data => {
            if(data === "Syncing successful") {
                saveSyncDate();
            }
            alert(data);
        }, error => alert(error));
    });

    jq("#custom").click(function (){
        jq("#custom_date").show();
        jq("#custom_sync").click(function (){
            var startDate = jq("#start").val();
            var endDate = jq("#end").val();
            if (startDate === "") {
                alert("Please choose a start date");
            } else if (endDate === "") {
                alert("Please choose an end date");
            } else {
                alert("Syncing patients from " + startDate + " to " + endDate);
                syncCustom(startDate, endDate).then(data => {
                    if(data === "Syncing successful") {
                        saveSyncDate();
                    }
                    alert(data);
                }, error => alert(error));
            }
        });
    });

    function syncUpdate() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromLastSync")}",
            dataType: "json"
        }));
    }

    function syncInitial() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromInitial")}",
            dataType: "json"
        }))
    }

    function syncCustom(from, to) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromCustomDate")}",
            dataType: "json",
            data: {
                'from': from,
                'to': to
            }
        }))
    }

    function saveSyncDate() {
        alert("Saving last sync")
        jq.ajax({
            url: "${ui.actionLink("saveLastSync")}",
            dataType: "json"
        })
    }
</script>