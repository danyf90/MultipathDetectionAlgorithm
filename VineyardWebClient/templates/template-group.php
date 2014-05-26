<form class="content entity" id="group">
	<input type="hidden" id="group-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input data-name="name" id="group-name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="group-table-body">
            <tr><th>Description</th><td><input id="group-description" data-name="description" type="text" placeholder="Insert the description..."/></td></tr>
        </tbody>
		<tbody id="group-workers">
		</tbody>
		<tfoot>
			<tr><th></th><td class="empty"><a href="#" id="group-add-worker">+ Add a worker</a></td></tr>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Done" /> <input type="button" id="control-cancel" value="Cancel"/></td></tr>
		</tfoot>
    </table>
    <script src="/js/group/single.js"></script>
</form>