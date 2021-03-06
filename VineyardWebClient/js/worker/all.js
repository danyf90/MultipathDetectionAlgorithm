var insertWorker = function (worker, $container) {
	var padding = 10;
	
    var roles = worker.role.replace(",", ", ");
    if (roles == undefined)
        roles = "Nessuno"
    
	var row = '<tr><td class="first-col" style="padding-left: ' + padding + 'px;">' +
		'<a href="/worker/' + worker.id + '">' + worker.name + '</a></td>' + 
		'<td>' + worker.username + '</td>' + 
		'<td>' + worker.email + '</td>' + 
		'<td>' + roles + '</td></tr>';
	
	$container.append(row);
};

var loadList = function () {
    
    var requestedUrl = vineyard.config.serverUrl + "worker/" ;
	
    $.getJSON(requestedUrl, function (workers) {
        $.each(workers, function (index, worker) {
            insertWorker(worker, $("#worker-all-table-body"));
        });
    });
};

loadList();