var insertGroup = function (group, $container) {
	var padding = 10;

	var row = '<tr><td class="first-col" style="padding-left: ' + padding + 'px;">' +
		'<a href="/group/' + group.id + '">' + group.name + '</a></td>' +
		'<td>' + group.description + '</td><td class="last-column">' + (group.workers != null ? group.workers.length : '0') + '</td></tr>';
	
	$row = $(row);
	$span = $('<span data-id="' + group.id + '" class="delete-row"></span>');
	$span.on("click", deleteGroup);
	
	$row.find("td").last().append($span);
	
    $container.append($row);
};

var deleteGroup = function () {
	// this = span
	$row = $(this).parents("tr");
	var requestedUrl = vineyard.config.serverUrl + "group/" + this.dataset.id;
	$.ajax({
		type: "DELETE",
		url: requestedUrl,
		success: function () { $row.remove(); }
	});
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