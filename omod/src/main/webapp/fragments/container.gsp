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
<br/>
<br/>
<table>
    <thead>
    <tr>
        <th>Created by</th>
        <th>Total patients processed</th>
        <th>Total patients</th>
        <th>Sync type</th>
        <th>Sync status</th>
        <th>Date started</th>
        <th>Date completed</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <%
        if (recentSyncBatches != null) {
            for (int i = 0; i < recentSyncBatches.size(); i++) {
    %>
    <tr>
        <td><%= recentSyncBatches.get(i).getOwnerUsername() %></td>
        <td><%= recentSyncBatches.get(i).getPatientsProcessed() %></td>
        <td><%= recentSyncBatches.get(i).getPatients() %></td>
        <td><%= recentSyncBatches.get(i).getSyncType() %></td>
        <td><%= recentSyncBatches.get(i).getStatus() %></td>
        <td><%= recentSyncBatches.get(i).getDateStarted() %></td>
        <td><%= recentSyncBatches.get(i).getDateCompleted() %></td>
        <td>
            <i style="font-size: 20px;" class="icon-play edit-action" title="resume"></i>
            <i style="font-size: 20px;" class="icon-remove edit-action" title="delete file"></i>
            <i style="font-size: 20px;" class="icon-refresh edit-action" title="rerun file"></i>
        </td>
    </tr>
    <%
            }
        }
    %>
    </tbody>
</table>

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
        window.onbeforeunload = function() {
            return "Dude, are you sure you want to leave? Think of the kittens!";
        };
        patientCountFromInitial().then(resp => {
            var count = resp.body;
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                getPatientsProcessed(count, "INITIAL").then(resp => {
                    var response = resp.body.split("/");
                    var start = parseInt(response[0]);
                    var id = response[1];
                    var length = 500;
                    alert("Syncing from " + start + " to " + count);
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        getPatientsProcessed(count, "INITIAL").then(resp => {
                            id = resp.body.split("/")[1];
                        });
                    }
                    batchSyncFromInitial(count, start, length, id);
                })

            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    });

    function batchSyncFromInitial(total, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= total) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Extracting from "+ start + " to " + (start + length)+ " of " + total +" patients</p>");
        }
        // jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
        //     "<p>Extracting from "+ start + " to " + (start + length)+ " of " + total +" patients</p>");
        syncInitial(total, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse !== "Sync complete!" &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                start = start + length;
                var remaining = total - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "INITIAL", id, total);
                batchSyncFromInitial(total, start, length, id);
            } else {
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }

        }, error => {
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

    jq("#update").click(function(){
        alert("Syncing from last sync date");

        patientCountFromLastSync().then(resp => {
            var count = resp.body;
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                getPatientsProcessed(count, "INCREMENTAL").then(resp => {
                    var response = resp.body.split("/");
                    var start = parseInt(response[0]);
                    var id = response[1];
                    var length = 500;
                    alert("Syncing from " + start + " to " + count);
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        getPatientsProcessed(count, "INCREMENTAL").then(resp => {
                            id = resp.body.split("/")[1];
                        });
                    }
                    batchSyncFromLastSync(count, start, length, id);
                })
                // batchSyncFromLastSync(count, start, length);
            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    });

    function batchSyncFromLastSync(totalPatients, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= totalPatients) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Extracting from "+ start + " to " + (start + length)+ " of " + totalPatients +" patients</p>");
        }
        syncUpdate(totalPatients, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse !== "Sync complete!" &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                start = start + length;
                var remaining = totalPatients - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "INCREMENTAL", id, totalPatients);
                batchSyncFromLastSync(totalPatients, start, length, id);
            } else {
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
            // if (serverResponse === "Sync complete!") {
            //     saveSyncDate();
            // }
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
                        getPatientsProcessed(count, "CUSTOM").then(resp => {
                            var response = resp.body.split("/");
                            var start = parseInt(response[0]);
                            var id = response[1];
                            var length = 500;
                            alert("Syncing from " + start + " to " + count);
                            jq('#overlay').fadeIn(300);
                            if (id === 0) {
                                getPatientsProcessed(count, "CUSTOM").then(resp => {
                                    id = resp.body.split("/")[1];
                                });
                            }
                            batchSyncFromCustomDate(startDate, endDate, count, start, length, id);
                        })
                        // batchSyncFromCustomDate(startDate, endDate, count, start, length);
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

    function batchSyncFromCustomDate(from, to, total, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= total) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Extracting from "+ start + " to " + (start + length)+ " of " + total +" patients</p>");
        }
        syncCustom(from, to, total, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse !== "Sync complete!" &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                start = start + length;
                var remaining = total - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "CUSTOM", id, total);
                batchSyncFromCustomDate(from, to, total, start, length, id);
            } else {
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
            // if (serverResponse === "Sync complete!") {
            //     saveSyncDate();
            // }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    function syncUpdate(total, start, length, id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromLastSync")}",
            dataType: "json",
            data: {
                'start': start,
                'length': length,
                'total': total,
                'id': id
            }
        }));
    }

    function syncInitial(total, start, length, id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromInitial")}",
            dataType: "json",
            data: {
                'start': start,
                'length': length,
                'total': total,
                'id': id
            }
        }))
    }

    function patientCountFromInitial() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCount")}",
            dataType: "json"
        }))
    }

    function getPatientsProcessed(total, type) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsProcessed")}",
            dataType: "json",
            data: {
                'total': total,
                'type': type
            }
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

    function syncCustom(from, to, total, start, length, id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromCustomDate")}",
            dataType: "json",
            data: {
                'from': from,
                'to': to,
                'start': start,
                'length': length,
                'total': total,
                'id': id
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

    function updateCdrSyncBatch(processed, type, id, total) {
        jq.ajax({
            url: "${ui.actionLink("updateCdrSyncBatch")}",
            dataType: "json",
            data: {
                'processed': processed,
                'type': type,
                'id': id,
                'total': total
            }
        })
    }

    let screenLock;

    function isScreenLockSupported() {
        return ('wakeLock' in navigator);
    }

    document.addEventListener('visibilitychange', async () => {
        if (screenLock !== null && document.visibilityState === 'visible') {
            screenLock = await navigator.wakeLock.request('screen');
        }
    });

    screenLock.onrelease = () => {
        console.log("Lock released 🎈");
    };
    // You can also use addEventListener
    screenLock.addEventListener('release', () => {
        console.log("Lock released 🎈");
    });
</script>