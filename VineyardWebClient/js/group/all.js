var insertGroup = function (group, $container) {
	var padding = 10;
	
	var row = '<tr><td class="first-col" style="padding-left: ' + padding + 'px;">' +
		'<a href="/group/' + group.id + '">' + group.name + '</a></td>' + 
		'<td>' + group.description + '</td>' + 
		'<td>' + group.email + '</td><td>';
    
    $.each(group.workers, function(index, worker) {
        row += worker + ", ";
    });
    
    if (row.charAt(row.length - 2) === ',')
        row = row.substring(0, row.length - 2);
    
    row += '</td></tr>';
    
    $container.append(row);
};

var loadList = function () {
    
    var requestedUrl = vineyard.config.serverUrl + "group/" ;
	
    $.getJSON(requestedUrl, function (groups) {
        
        $.each(groups, function (index, group) {
            insertGroup(group, $("#group-all-table-body"));
        });
		
		$("#loading").css("visibility", "hidden"); 
    });
};

loadList();