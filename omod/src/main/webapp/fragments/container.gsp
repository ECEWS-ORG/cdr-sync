<%
    ui.includeCss("cdrsync", "style.css")
    ui.includeCss("cdrsync", "cardPage.css")

%>
<%= ui.resourceLinks() %>
<script src="https://rawgit.com/schmich/instascan-builds/master/instascan.min.js"></script>
<section class="home-content-top">
    <div class="container">

        <!--our-quality-shadow-->
        <div class="clearfix"></div>
        <h1 class="heading1">Welcome to CDR-SYNC Module</h1>
        <div class="tabbable-panel margin-tops4 ">
            <div class="tabbable-line">
                <ul class="nav nav-tabs tabtop  tabsetting">
                    <li class="active"> <a href="#tab_default_1" data-toggle="tab" id="scan"> Card Scanner </a> </li>
                    <li> <a href="#tab_default_2" data-toggle="tab" id="extract"> CDR Extraction </a> </li>
                </ul>
                <div class="tab-content margin-tops">
                    <div class="flex-container" id="card-mapper-details">
                        <div>
                            <p style="color: red"><b><%= patientsOnArt %> Patients on ART</b></p>
                        </div>
                        <div>
                            <p style="color: green"><b><%= patientsMappedToNfc %> Patients Mapped to NFC Card</b></p>
                        </div>
                        <div>
                            <p style="color: blue"><b><%= percentageMapped %>% Patients Mapped to NFC Card</b></p>
                        </div>
                    </div>
                    <div id="nfc-message"></div>
                    <div class="tab-pane active fade in" id="tab_default_1">
                        <div class="col-md-8 card-button-container">
                            <div style="width:50%">
                                <button id="card_scanner" style="color: red"><b>Scan Card</b></button>
                                <div id="pepfar_id" style="width:50%">
                                    <input type="text" id="pepfar" placeholder="Input patient's first name, last name, or pepfar Id"
                                           style="color: #333;background-color: #f0f0f0;border: 1px solid #ccc;
                                           border-radius: 5px;width: 500px;height: 50px">
                                </div>
                            </div>
                            <div style="width:50%">
                                <button id="qr_scanner" style="color: red"><b>Scan QR</b></button>
                                <video id="video" style="width: 100%;margin-top: 10px;"></video>
                            </div>

                        </div>
                            <table id="patients">
                                <thead>
                                <tr>
                                    <th>Patient Name</th>
                                    <th>Phone Number</th>
                                    <th>Patient Identifier</th>
                                    <th>Identifier Type</th>
                                    <th>Voided</th>
                                    <th>Action</th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                    </div>
                    <div class="tab-pane fade" id="tab_default_2">
                        <div id="overlay">
                            <div class="cv-spinner">
                                <span class="spinner"></span>
                            </div>
                        </div>
                        <div class="container-wrap">
                            <h3>Last Sync Date: <span id="lastSyncDate"><%= lastSyncDate != null ? lastSyncDate : "N/A" %></span></h3>
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
                        <table id="recent-sync-batches">
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
                                    <%
                                            if (recentSyncBatches.get(i).getDateCompleted() == null) {
                                                Date syncStartDate = recentSyncBatches.get(i).getSyncStartDate();
                                                Date syncEndDate = recentSyncBatches.get(i).getSyncEndDate();
                                                int totalPatients = recentSyncBatches.get(i).getPatients();
                                                int start = recentSyncBatches.get(i).getPatientsProcessed();
                                                int length = 500;
                                                int id = recentSyncBatches.get(i).getId();
                                                String syncType = recentSyncBatches.get(i).getSyncType();
                                    %>
                                    <i style="font-size: 20px;" class="icon-play edit-action" title="resume"
                                       onclick="resume('<%= syncStartDate %>', '<%= syncEndDate %>', '<%= totalPatients %>', '<%= start %>',
                                           '<%= length %>', '<%= id %>', '<%= syncType %>')"></i>
                                    <%
                                            }
                                    %>
                                    <i style="font-size: 20px;" class="icon-remove edit-action" title="delete file" onclick="deleteBatch('<%= recentSyncBatches.get(i).getId() %>')"></i>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>


<script type="text/javascript">
    var jq = jQuery;
    jq("#custom_date").hide();
    jq("#pepfar_id").hide();
    jq("#patients").hide();
    jq("#video").hide();
    jq("#card-mapper-details").hide();

    let isCardScannerActive = true;

    let nfcCardId = null;

    document.addEventListener('DOMContentLoaded', function () {
        readNfcCardAtInterval();
    });

    function readNfcCardAtInterval() {
        if (isCardScannerActive) {
            let timer = setInterval(function () {
                readNfcCard().then(resp => {
                    console.log(resp.body);
                    let status = resp.body["Status"];
                    if (status === true) {
                        clearInterval(timer);
                        alert(resp.body["Message"])
                        nfcCardId = resp.body["Data"];
                        getNfcCardMapperByNfcCardId(nfcCardId).then(resp => {
                            var nfcCardMapperByNfcCardId = resp.body;
                            console.log(nfcCardMapperByNfcCardId);
                            if (nfcCardMapperByNfcCardId["Data"] !== null) {
                                console.log("Card already mapped to a patient");
                                var url = nfcCardMapperByNfcCardId["Data"];
                                redirectToPatientDashboard(url);
                            } else {
                                if (confirm("Card not mapped to a patient, do you want to map it?") === false) {
                                    return;
                                }
                                jq("#pepfar_id").show();
                                jq("#video").hide();
                                var element = document.getElementById("scan");
                                element.click();
                                jq("#pepfar").keyup(function () {
                                    var pepfarId = jq.trim(this.value);
                                    if (pepfarId !== "") {
                                        getPatientDetails(pepfarId).then(resp => {
                                            var patient = resp.body;
                                            if (patient["Data"] !== null) {
                                                jq("#patients").show();
                                                jq("#patients tbody").empty();
                                                var patientDetails = patient["Data"];
                                                console.log(patientDetails);
                                                if (patientDetails.length <= 30) {
                                                    jq.each(patientDetails, function (key, value) {
                                                        jq("#patients tbody").append("<tr>" +
                                                            "<td>" + value.patientName + "</td>" +
                                                            "<td>" + value.patientPhoneNumber + "</td>" +
                                                            "<td>" + value.patientIdentifier + "</td>" +
                                                            "<td>" + value.identifierType + "</td>" +
                                                            "<td>" + value.voided + "</td>" +
                                                            "<td><button onclick=mapCardToPatient('" + nfcCardId + "','" +
                                                            value.patientIdentifier + "','" + value.patientPhoneNumber + "','" +
                                                            value.patientUuid + "')>Map</button></td>" +
                                                            "</tr>");
                                                    });
                                                }

                                            } else {
                                                alert("Patient not found");
                                            }
                                        }, error => {
                                            console.log(error);
                                            alert(error.statusText);
                                        });
                                    }
                                })


                                // jq("#pepfar_submit").click(function () {
                                //     var pepfarId = jq("#pepfar").val();
                                //     if (pepfarId === "") {
                                //         alert("Please enter a pepfar id");
                                //     } else {
                                //         getNfcCardMapperByPepfarId(pepfarId).then(resp => {
                                //             var nfcCardMapperByPepfarId = resp.body;
                                //             console.log(nfcCardMapperByPepfarId);
                                //             if (nfcCardMapperByPepfarId["data"] !== null) {
                                //                 alert("Pepfar id already mapped to a card");
                                //             } else {
                                //                 getPatientDetails(pepfarId).then(resp => {
                                //                     var patient = resp.body;
                                //                     console.log(patient);
                                //                     if (patient["data"] !== null) {
                                //                         var patientDetails = patient["data"];
                                //                         console.log(patientDetails);
                                //                         var patientPhoneNo = patientDetails.patientPhoneNumber;
                                //                         console.log(patientPhoneNo);
                                //                         var patientUuid = patientDetails.patientUuid;
                                //                         console.log(patientUuid);
                                //                         var patientName = patientDetails.patientName
                                //                         saveNfcCardMapper(nfcCardId, pepfarId, patientPhoneNo, patientUuid, patientName).then(resp => {
                                //                             var response = resp.body;
                                //                             console.log(response);
                                //                             if (response["message"] === "Successful") {
                                //                                 alert("Card "+nfcCardId+" successfully mapped to " + pepfarId);
                                //                                 redirectToPatientDashboard(response["data"]);
                                //                             } else {
                                //                                 alert("Failed to map card to " + pepfarId);
                                //                             }
                                //                         }, error => {
                                //                             console.log(error);
                                //                             alert(error.statusText);
                                //                         });
                                //                     } else {
                                //                         alert("Patient not found");
                                //                     }
                                //                 }, error => {
                                //                     console.log(error);
                                //                     alert(error.statusText);
                                //                 });
                                //             }
                                //         }, error => {
                                //             console.log(error);
                                //             alert(error.statusText);
                                //         });
                                //     }
                                // });
                            }
                        }, error => {
                            console.log(error);
                            alert(error.statusText);
                        });
                    } else {
                        console.log(resp.body["Message"]);
                        document.getElementById("nfc-message").innerHTML = resp.body["Message"];
                    }
                })
            }, 5000);

            jq("#scan").click(function () {
                jq("#card-mapper-details").show();
                isCardScannerActive = true;
            });

            jq("#extract").click(function () {
                isCardScannerActive = false;
                jq("#card-mapper-details").hide();
                jq("#nfc-message").hide();
                clearInterval(timer);
            });
        }
    }

    jq("#card_scanner").click(function () {
        readNfcCardAtInterval();
    });

    function mapCardToPatient(nfcCardId, pepfarId, patientPhoneNo, patientUuid) {
        getNfcCardMapperByPepfarId(pepfarId).then(resp => {
            var nfcCardMapperByPepfarId = resp.body;
            console.log(nfcCardMapperByPepfarId);
            if (nfcCardMapperByPepfarId["Data"] !== null) {
                alert("Patient already mapped to a card");
            } else {
                saveNfcCardMapper(nfcCardId, pepfarId, patientPhoneNo, patientUuid).then(resp => {
                    var response = resp.body;
                    console.log(response);
                    if (response["Message"] === "Successful") {
                        alert("Card "+nfcCardId+" successfully mapped to " + pepfarId);
                        redirectToPatientDashboard(response["Data"]);
                    } else {
                        alert("Failed to map card to " + pepfarId);
                    }
                }, error => {
                    console.log(error);
                    alert(error.statusText);
                });
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    jq("#qr_scanner").click(function () {
        jq("#pepfar_id").hide();
        jq("#video").show();
        jq("#patients").hide();
        navigator.mediaDevices.getUserMedia({ video: true })
            .then(stream => {
                const videoElement = document.getElementById('video');
                videoElement.srcObject = stream;

                // Initialize the QR code scanner
                const scanner = new Instascan.Scanner({ video: videoElement });
                scanner.addListener('scan', content => {
                    console.log('QR Code detected:', content);
                    // You can perform any action you want with the scanned content
                    // For example, redirecting to a URL
                    redirectToPatientDashboard(content);
                });

                // Start scanning
                Instascan.Camera.getCameras()
                    .then(cameras => {
                        if (cameras.length > 0) {
                            scanner.start(cameras[0]); // Start scanning using the first available camera
                        } else {
                            console.error('No cameras found.');
                        }
                    })
                    .catch(error => {
                        console.error('Error accessing cameras:', error);
                    });
            })
            .catch(error => {
                console.error('Error accessing webcam:', error);
            });
    })


    jq("#patientDetails").click(function (){
        var patientIdentifier = prompt("Please enter patient identifier", "");
        if (patientIdentifier != null) {
            getPatientDetails(patientIdentifier).then(resp => {
                var patient = resp.body;
                console.log(patient)
                if (patient !== "") {
                    alert("Patient details: " + patient);
                } else {
                    alert("Patient not found");
                }
            }, error => {
                console.log(error);
                alert(error.statusText);
            });
        }
    });

    jq("#initial").click(function(){
        alert("Syncing from inception");
        window.onbeforeunload = function() {
            return "Dude, are you sure you want to leave? Think of the kittens!";
        };
        syncPatientsFromInitial();
    });
    // <i style="font-size: 20px;" class="icon-refresh edit-action" title="rerun file"></i>
    jq("#update").click(function(){
        alert("Syncing from last sync date");
        window.onbeforeunload = function() {
            return "Dude, are you sure you want to leave? Think of the kittens!";
        };
        syncPatientsFromLastSync();
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
                window.onbeforeunload = function() {
                    return "Dude, are you sure you want to leave? Think of the kittens!";
                };
                syncPatientsFromCustomDate(startDate, endDate);
            }
        });
    });
    function resume(startDate, endDate, totalPatients, start, length, id, syncType) {
        alert("Resuming from where it stopped");
        window.onbeforeunload = function() {
            return "Dude, are you sure you want to leave? Think of the kittens!";
        };
        totalPatients = parseInt(totalPatients);
        start = parseInt(start);
        length = parseInt(length) >= totalPatients ? totalPatients : parseInt(length);
        id = parseInt(id);
        jq('#overlay').fadeIn(300);
        switch (syncType) {
            case "INITIAL":
                batchSyncFromInitial(totalPatients, start, length, id);
                break;
            case "INCREMENTAL":
                batchSyncFromLastSync(startDate, endDate, totalPatients, start, length, id);
                break;
            case "CUSTOM":
                batchSyncFromCustomDate(startDate, endDate, totalPatients, start, length, id);
                break;
            default:
                break;
        }
    }
    function updateRecentSyncBatches() {
        getRecentSyncBatches().then(resp => {
            var recentSyncBatches = resp.body;
            console.log(recentSyncBatches);
            jq("#recent-sync-batches tbody").empty();
            for (var i = 0; i < recentSyncBatches.length; i++) {
                var dateCompleted = recentSyncBatches[i].dateCompleted;
                dateCompleted = dateCompleted === null ? "" : new Date(dateCompleted);
                var dateStarted = new Date(recentSyncBatches[i].dateStarted);
                dateCompleted = dateCompleted === "" ? "" : formatDateTime(dateCompleted);
                dateStarted = formatDateTime(dateStarted);
                var resumeButton = dateCompleted === "" ? "<i style='font-size: 20px;' class='icon-play edit-action' title='resume' onclick='resume(" + recentSyncBatches[i].syncStartDate + ", " + recentSyncBatches[i].syncEndDate + ", " + recentSyncBatches[i].patients + ", " + recentSyncBatches[i].patientsProcessed + ", 500, " + recentSyncBatches[i].id + ", \"" + recentSyncBatches[i].syncType + "\")'></i>" : "";
                jq("#recent-sync-batches tbody").append("<tr>" +
                    "<td>" + recentSyncBatches[i].ownerUsername + "</td>" +
                    "<td>" + recentSyncBatches[i].patientsProcessed + "</td>" +
                    "<td>" + recentSyncBatches[i].patients + "</td>" +
                    "<td>" + recentSyncBatches[i].syncType + "</td>" +
                    "<td>" + recentSyncBatches[i].status + "</td>" +
                    "<td>" + dateStarted + "</td>" +
                    "<td>" + dateCompleted + "</td>" +
                    "<td>" +
                    resumeButton +
                    "<i style='font-size: 20px;' class='icon-remove edit-action' title='delete file' onclick='deleteBatch(" + recentSyncBatches[i].id + ")'></i>" +
                    "</td>" +
                    "</tr>"
                );
            }
        });
        getLastSyncDate().then(resp => {
            jq("#lastSyncDate").html(resp.body);
        });
    }
    function deleteBatch(id) {
        if (confirm("Are you sure you want to delete this batch?")) {
            jq('#overlay').fadeIn(300);
            deleteCdrSyncBatch(id).then(resp => {
                jq('#overlay').fadeOut(300);
                alert(resp.body);
                updateRecentSyncBatches();
            }, error => {
                jq('#overlay').fadeOut(300);
                console.log(error);
                alert(error.statusText);
            });
        } else {
            alert("Delete cancelled");
        }
    }
    function syncPatientsFromInitial() {
        patientCountFromInitial().then(resp => {
            let totalPatients = resp.body;
            console.log("Total patients to sync: " + totalPatients);
            if (totalPatients > 0) {
                getPatientsProcessed(totalPatients, "INITIAL").then(resp => {
                    const response = resp.body.split("/");
                    const start = parseInt(response[0]);
                    let id = parseInt(response[1]);
                    const remaining = totalPatients - start;
                    const length = remaining < 500 ? remaining : 500;
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        alert("Syncing from " + start + " to " + totalPatients);
                        getPatientsProcessed(totalPatients, "INITIAL").then(resp => {
                            id = parseInt(resp.body.split("/")[1]);
                            batchSyncFromInitial(totalPatients, start, length, id);
                        });
                    } else {
                        if (confirm("There's an incomplete sync, do you want to resume from where it stopped?")) {
                            totalPatients = parseInt(response[3]);
                            let syncType = response[2];
                            switch (syncType) {
                                case "INITIAL":
                                    batchSyncFromInitial(totalPatients, start, length, id);
                                    break;
                                case "INCREMENTAL":
                                    batchSyncFromLastSync(totalPatients, start, length, id);
                                    break;
                                default:
                                    break;
                            }
                            // batchSyncFromInitial(totalPatients, start, length, id);
                        } else {
                            updateCdrSyncBatchToCancelled(id);
                            syncPatientsFromInitial();
                            jq('#overlay').fadeOut(300);
                        }
                        // alert("There's an incomplete sync, resuming from where it stopped");
                        // batchSyncFromInitial(totalPatients, start, length, id);
                        // jq('#overlay').fadeOut(300);
                    }
                })

            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    function batchSyncFromInitial(total, start, length, id) {
        let serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= total) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            const percentage = Math.round(((start + length) / total) * 100);
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>"+ percentage + "% of " + total +" patients</p>");
        }
        syncInitial(total, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse.indexOf("Sync complete!") === -1 &&
                serverResponse !== "There's a problem extracting data from the database, kindly contact the system administrator!" &&
                serverResponse !== "Incomplete syncing, try again later!" &&
                serverResponse !== "Cannot resume sync, kindly start a new sync!")
            {
                start = start + length;
                var remaining = total - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "INITIAL", id, total);
                batchSyncFromInitial(total, start, length, id);
            } else if (serverResponse.indexOf("Sync complete!") >= 0) {
                createDownloadButton(serverResponse);
                updateRecentSyncBatches();
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
            } else {
                updateRecentSyncBatches();
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }

        }, error => {
            updateRecentSyncBatches();
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

    function syncPatientsFromLastSync() {
        var currentDate = new Date();
        console.log(currentDate);
        var endDate = formatDate(currentDate);
        console.log("end date:"+endDate);
        patientCountFromLastSync().then(resp => {
            var response = resp.body.split("/");
            var count = parseInt(response[1]);
            var startDate = response[0];
            console.log("start date:" + startDate);
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                getPatientsProcessed(count, "INCREMENTAL").then(resp => {
                    var response = resp.body.split("/");
                    var start = parseInt(response[0]);
                    var id = parseInt(response[1]);
                    var length = (count-start) < 500 ? count-start : 500;
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        alert("Syncing from " + start + " to " + count);
                        getPatientsProcessed(count, "INCREMENTAL").then(resp => {
                            id = parseInt(resp.body.split("/")[1]);
                            batchSyncFromLastSync(startDate, endDate, count, start, length, id)
                        });
                    } else {
                        if (confirm("There's an incomplete sync, do you want to resume from where it stopped?")) {
                            count = parseInt(response[3]);
                            let syncType = response[2];
                            switch (syncType) {
                                case "INITIAL":
                                    batchSyncFromInitial(count, start, length, id);
                                    break;
                                case "INCREMENTAL":
                                    batchSyncFromLastSync(startDate, endDate, count, start, length, id);
                                    break;
                                default:
                                    break;
                            }
                            // batchSyncFromInitial(totalPatients, start, length, id);
                        } else {
                            updateCdrSyncBatchToCancelled(id);
                            syncPatientsFromLastSync();
                            jq('#overlay').fadeOut(300);
                        }
                        // alert("There's an incomplete sync, resuming from where it stopped");
                    }
                })
            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    function formatDate(date) {
        var d = new Date(date),
            month = '' + (d.getMonth() + 1),
            day = '' + d.getDate(),
            year = d.getFullYear();

        if (month.length < 2)
            month = '0' + month;
        if (day.length < 2)
            day = '0' + day;

        return [year, month, day].join('-');
    }

    function formatDateTime(date) {
        var d = new Date(date),
            month = '' + (d.getMonth() + 1),
            day = '' + d.getDate(),
            year = d.getFullYear(),
            hour = '' + d.getHours(),
            minute = '' + d.getMinutes(),
            second = '' + d.getSeconds();
        if (month.length < 2)
            month = '0' + month;
        if (day.length < 2)
            day = '0' + day;
        if (hour.length < 2)
            hour = '0' + hour;
        if (minute.length < 2)
            minute = '0' + minute;
        if (second.length < 2)
            second = '0' + second;
        return [year, month, day].join('-') + " " + [hour, minute, second].join(':');
    }

    function batchSyncFromLastSync(startDate, endDate, totalPatients, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= totalPatients) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            var percentage = Math.round(((parseInt(start)+parseInt(length))/parseInt(totalPatients))*100);
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>"+ percentage + "% of " + totalPatients +" patients</p>");
        }
        if (parseInt(start) === 0) {
            alert("Syncing from " + startDate + " to " + endDate);
            updateCdrSyncBatchStartAndEndDate(id, startDate, endDate);
        }
        syncUpdate(totalPatients, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse.indexOf("Sync complete!") === -1  &&
                serverResponse !== "There's a problem extracting data from the database, kindly contact the system administrator!" &&
                serverResponse !== "Incomplete syncing, try again later!" &&
                serverResponse !== "Cannot resume sync, kindly start a new sync!")
            {
                start = parseInt(start) + parseInt(length);
                var remaining = parseInt(totalPatients) - parseInt(start);
                if (remaining < parseInt(length)) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "INCREMENTAL", id, totalPatients);
                batchSyncFromLastSync(startDate, endDate, totalPatients, start, length, id);
            } else if (serverResponse.indexOf("Sync complete!") >= 0) {
                createDownloadButton(serverResponse);
                updateRecentSyncBatches();
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
            } else {
                updateRecentSyncBatches();
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
        }, error => {
            updateRecentSyncBatches();
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

    function syncPatientsFromCustomDate(startDate, endDate) {
        patientCountFromCustomDate(startDate, endDate).then(resp => {
            var count = resp.body;
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                getPatientsProcessed(count, "CUSTOM").then(resp => {
                    var response = resp.body.split("/");
                    var start = parseInt(response[0]);
                    var id = parseInt(response[1]);
                    var length = (count-start) < 500 ? count-start : 500;
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        alert("Syncing from " + start + " to " + count);
                        getPatientsProcessed(count, "CUSTOM").then(resp => {
                            id = parseInt(resp.body.split("/")[1]);
                            batchSyncFromCustomDate(startDate, endDate, count, start, length, id);
                        });
                    } else {
                        if (confirm("There's an incomplete sync, do you want to resume from where it stopped?")) {
                            count = parseInt(response[3]);
                            let syncType = response[2];
                            switch (syncType) {
                                case "INITIAL":
                                    batchSyncFromInitial(count, start, length, id);
                                    break;
                                case "INCREMENTAL":
                                    batchSyncFromLastSync(count, start, length, id);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            updateCdrSyncBatchToCancelled(id);
                            syncPatientsFromCustomDate(startDate, endDate);
                            jq('#overlay').fadeOut(300);
                        }
                    }
                })
            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    function batchSyncFromCustomDate(startDate, endDate, total, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= total) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            var percentage = Math.round(((start+length)/total)*100);
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>"+ percentage + "% of " + total +" patients</p>");
        }
        if (start === 0) {
            alert("Syncing from " + startDate + " to " + endDate);
            updateCdrSyncBatchStartAndEndDate(id, startDate, endDate);
        }
        syncCustom(startDate, endDate, total, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse.indexOf("Sync complete!") === -1 &&
                serverResponse !== "There's a problem extracting data from the database, kindly contact the system administrator!" &&
                serverResponse !== "Incomplete syncing, try again later!" &&
                serverResponse !== "Cannot resume sync, kindly start a new sync!")
            {
                start = start + length;
                var remaining = total - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "CUSTOM", id, total);
                batchSyncFromCustomDate(startDate, endDate, total, start, length, id);
            } else if (serverResponse.indexOf("Sync complete!") >= 0) {
                createDownloadButton(serverResponse);
                updateRecentSyncBatches();
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
            } else {
                updateRecentSyncBatches();
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
        }, error => {
            updateRecentSyncBatches();
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

    function createDownloadButton(serverResponse) {
        var response = serverResponse.split("#");
        serverResponse = response[0];
        var zipFiles = response[1].split("&&");
        console.log(zipFiles);
        jq('#message').html("<p>"+serverResponse+"</p>" +
            "<p>Click the download button(s) below to download the extracted files</p><br>"
        );
        for (var i = 0; i < zipFiles.length; i++) {
            // var id = "download"+i;
            if (zipFiles[i] !== "") {
                let fileName = zipFiles[i];
                if (fileName.indexOf("errorReport") >= 0) {
                    jq('#message').append("<button onclick=downloadFile('" + zipFiles[i] +"') >Download error file</button>");
                } else {
                    jq('#message').append("<button onclick=downloadFile('" + zipFiles[i] +"') >Download file</button>");
                }
                // jq('#message').append("<button onclick=downloadFile('" + zipFiles[i] +"') >Download file</button>");
                // document.getElementById(id).click();
            }
        }
    }

    function downloadFile(fileName) {
        window.location = fileName;
    }

    function redirectToPatientDashboard(url) {
        window.location.href = url;
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

    function getNfcCardMapperByNfcCardId(nfcCardId) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "getNfcCardMapperByNfcCardId")}",
            dataType: "json",
            data: {
                'nfcCardId': nfcCardId
            }
        }))
    }

    function getNfcCardMapperByPepfarId(pepfarId) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "getNfcCardByPatientIdentifier")}",
            dataType: "json",
            data: {
                'patientIdentifier': pepfarId
            }
        }))
    }

    function saveNfcCardMapper(nfcCardId, pepfarId, patientPhoneNo, patientUuid) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "saveNfcCardMapper")}",
            dataType: "json",
            data: {
                'nfcCardId': nfcCardId,
                'patientIdentifier': pepfarId,
                'patientPhoneNo': patientPhoneNo,
                'patientUuid': patientUuid
            },
            beforeSend: function () {
                return confirm("Are you sure you want to map patient with pepfar Id (" + pepfarId + ") with a new card?")
            }
        }))
    }

    function getPatientDetails(patientIdentifier) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "getPatientDetails")}",
            dataType: "json",
            data: {
                'patientIdentifier': patientIdentifier
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
        jq.ajax({
            url: "${ui.actionLink("saveLastSync")}",
            dataType: "json"
        })
    }

    function updateCdrSyncBatchStartAndEndDate(id, startDate, endDate) {
        jq.ajax({
            url: "${ui.actionLink("updateCdrSyncBatchStartAndEndDate")}",
            dataType: "json",
            data: {
                'id': id,
                'startDate': startDate,
                'endDate': endDate
            }
        })
    }

    function readNfcCard() {
        console.log("Reading nfc card")
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "readNfcCard")}",
            dataType: "json"
        }));
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

    function updateCdrSyncBatchToCancelled(id) {
        jq.ajax({
            url: "${ui.actionLink("updateCdrSyncBatchToCancelled")}",
            dataType: "json",
            data: {
                'id': id
            }
        })
    }
    function getRecentSyncBatches() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getRecentSyncBatches")}",
            dataType: "json"
        }))
    }
    function getLastSyncDate() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getLastSyncDate")}",
            dataType: "json"
        }))
    }
    function deleteCdrSyncBatch(id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("deleteCdrSyncBatch")}",
            dataType: "json",
            data: {
                'id': id
            }
        }));
    }

</script>