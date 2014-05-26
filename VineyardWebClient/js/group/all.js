var insertGroup = function (group, $container) {
	var padding = 10;
	
	var row = '<tr><td class="first-col" style="padding-left: ' + padding + 'px;">' +
		'<a href="/group/' + group.id + '">' + group.name + '</a></td>' + 
		'<td>' + group.description + '</td>' + 
		'<td>' + group.workers.length + '</td></tr>';

    $container.append(row);
};

var loadList = function () {
    
    var requestedUrl = vineyard.config.serverUrl + "group/" ;
    $.getJSON(requestedUrl, function (groups) {
        $.each(groups, function (index, group) {
            insertGroup(group, $("#group-all-table-body"));
        });
    });
};

loadList();