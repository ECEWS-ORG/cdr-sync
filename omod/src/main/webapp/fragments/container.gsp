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
    <h3>Last Sync Date: <%= lastSyncDate %></h3>
    <div id="message"></div>
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

    jq(document).ajaxStart(function() {
        jq("#overlay").fadeIn(300);
    }).ajaxComplete(function (){
        setTimeout(function(){
            jq("#overlay").fadeOut(300);
        },500);
    });

    jq("#initial").click(function(){
        alert("Syncing from inception");
        patientCountFromInitial().then(resp => {
            var count = resp.body;
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                var start = 0;
                var length = 500;
                batchSyncFromInitial(count, start, length);
            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
        // var start = 0;
        // var length = 500;
        // batchSyncFromInitial(start, length);

    });

    function batchSyncFromInitial(total, start, length) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));
        syncInitial(total, start, length).then(resp => {
            serverResponse = resp.body;
            if (serverResponse !== "Sync complete!" &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!") {
                batchSyncFromInitial(total, start + length, length);
            } else {
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
            if (serverResponse === "Sync complete!") {
                saveSyncDate();
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
        // return serverResponse;
    }

    jq("#update").click(function(){
        alert("Syncing from last sync date");

        patientCountFromLastSync().then(resp => {
            var count = resp.body;
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                var start = 0;
                var length = 500;
                batchSyncFromLastSync(count, start, length);
            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });

        // batchSyncFromLastSync(start, length);

        // syncUpdate().then(resp => {
        //     console.log(resp);
        //     if(resp.body === "Sync successful!") {
        //         saveSyncDate();
        //     }
        //     // alert(data);
        //     jq('#message').html("<p>"+resp.body+"</p>");
        // }, error => console.log(error));
    });

    function batchSyncFromLastSync(totalPatients, start, length) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));
        syncUpdate(totalPatients, start, length).then(resp => {
            serverResponse = resp.body;
            if (serverResponse !== "Sync complete!" &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                batchSyncFromLastSync(totalPatients, start + length, length);
            } else {
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
            if (serverResponse === "Sync complete!") {
                saveSyncDate();
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

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
                patientCountFromCustomDate(startDate, endDate).then(resp => {
                    var count = resp.body;
                    console.log("Total patients to sync: " + count);
                    if (count > 0) {
                        var start = 0;
                        var length = 500;
                        batchSyncFromCustomDate(startDate, endDate, count, start, length);
                    } else {
                        alert("No new patients to sync");
                    }
                }, error => {
                    console.log(error);
                    alert(error.statusText);
                });
                // var start = 0;
                // var length = 500;
                //
                // batchSyncFromCustomDate(startDate, endDate, start, length);

                // syncCustom(startDate, endDate).then(resp => {
                //     if(resp.body === "Sync successful!") {
                //         saveSyncDate();
                //     }
                //     // alert(data);
                //     jq('#message').html("<p>"+resp.body+"</p>");
                // }, error => console.log(error));
            }
        });
    });

    function batchSyncFromCustomDate(from, to, total, start, length) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));
        syncCustom(from, to, total, start, length).then(resp => {
            serverResponse = resp.body;
            if (serverResponse !== "Sync complete!" &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!") {
                batchSyncFromCustomDate(from, to, total, start + length, length);
            } else {
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
            if (serverResponse === "Sync complete!") {
                saveSyncDate();
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    function syncUpdate(total, start, length) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromLastSync")}",
            dataType: "json",
            data: {
                'start': start,
                'length': length,
                'total': total
            }
        }));
    }

    function syncInitial(total, start, length) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromInitial")}",
            dataType: "json",
            data: {
                'start': start,
                'length': length,
                'total': total
            }
        }))
    }

    function patientCountFromInitial() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCount")}",
            dataType: "json"
        }))
    }

    function patientCountFromLastSync() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCountFromLastSync")}",
            dataType: "json"
        }))
    }

    function patientCountFromCustomDate(from, to) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCountFromCustomDate")}",
            dataType: "json",
            data: {
                'from': from,
                'to': to
            }
        }))
    }

    function syncCustom(from, to, total, start, length) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromCustomDate")}",
            dataType: "json",
            data: {
                'from': from,
                'to': to,
                'start': start,
                'length': length,
                'total': total
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