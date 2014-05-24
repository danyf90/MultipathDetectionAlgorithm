<form class="content entity" id="task">
	<input type="hidden" id="task-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input data-name="title" id="task-title" placeholder="Insert a title..." type="text" /></h2></td></tr>
			<tr id="changes"><td></td>
				<td>Last changed: <span id="change-date"></span> by <span id="modifier"></span></td>
			</tr>
        </thead>
        <tbody id="task-table-body">
            <tr><th>Description</th><td><input data-name="description" id="task-description" type="text" placeholder="Insert a description..."/></td></tr>
			<tr><th>Status</th><td id="task-status"></td></tr>
			<tr><th>Priority</th><td id="task-priority"><select data-name="priority" id="task-priority">
				<option>-- Not Set --</option>
				<option value="low">low</option>
				<option value="medium">medium</option>
				<option value="high">high</option>
			</select></td></tr>
            <tr><th>Place</th><td id="task-place"><select id="task-place" data-name="place"></select></tr>
			<tr><th>Location</th><td id="task-location-link"><a href="#">Insert a location ...</a></td></tr>
			<tr><th id="task-assign-th">Assigned to <span>on datetime..</span></th><td id="task-assigned-to">
				<select data-name="assigned">
					<option>-- Nobody --</option>
					<optgroup label="Gruppo" id="task-assign-group"></optgroup>
					<optgroup label="Lavoratore" id="task-assign-worker"></optgroup>
				</select>
			</td></tr>
			<!-- Formato umano da leggere -->
			<tr><th>Deadline</th><td><input data-name="due_time" id="task-due-time" type="date" /></td></tr>
        </tbody>
		<tfoot>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Done" /> <input type="button" id="control-cancel" value="Cancel"/></td></tr>
		</tfoot>
    </table>
	<div class="right-side" id="task-revisions">
		<h3>Revisions</h3>
		<ul>
			<li><a href="/task/<?php $this->id; ?>">Current Revision</a></li>
		</ul>
    </div>
    <script src="/js/task/single.js"></script>
</form>