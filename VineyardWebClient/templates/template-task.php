<form class="content entity task-issue" id="task">
	<input type="hidden" id="task-id" value="<?php $this->id; ?>"/>
	<input type="hidden" id="task-modifier" name="modifier"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input data-name="title" id="task-title" placeholder="Insert a title..." type="text" /></h2></td></tr>
			<tr id="changes"><td></td>
				<td>Last changed: <span id="change-date"></span> by <a id="modifier"></a></td>
			</tr>
        </thead>
        <tbody id="task-table-body">
            <tr><th>Description</th><td><input data-name="description" id="task-description" type="text" placeholder="Insert a description..."/></td></tr>
			<tr><th>Status</th><td class="status" id="task-status"></td></tr>
			<tr><th>Priority</th><td><select data-name="priority" id="task-priority">
				<option class="priority">-- Not Set --</option>
				<option class="priority low" value="low">low</option>
				<option class="priority medium" value="medium">medium</option>
				<option class="priority high" value="high">high</option>
			</select></td></tr>
            <tr><th>Place</th><td><select id="task-place" data-name="place"></select></tr>
			<tr><th id="task-assign-th">Assigned to</th><td>
				<select id="task-assigned-to">
					<option>-- Nobody --</option>
					<optgroup label="Gruppo" data-name="assigned_group" id="task-assign-group"></optgroup>
					<optgroup label="Lavoratore" data-name="assigned_worker" id="task-assign-worker"></optgroup>
				</select>
			</td></tr>
			<!-- Formato umano da leggere -->
			<tr><th>Deadline</th><td><input data-name="due_time" id="task-due-time" type="date" /></td></tr>
        </tbody>
		<tfoot>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Done" /> <input type="button" id="control-cancel" value="Cancel"/></td></tr>
		</tfoot>
    </table>
	<div class="right-side" id="revisions">
		<h3>Revisions</h3>
		<ul>
			<li><a href="/task/<?php $this->id; ?>">Current Revision</a></li>
		</ul>
    </div>
    <script src="/js/task/single.js"></script>
</form>