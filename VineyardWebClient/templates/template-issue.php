<form class="content entity task-issue" id="issue">
	<input type="hidden" id="issue-id" value="<?php $this->id; ?>"/>
	<input type="hidden" id="issue-modifier" name="modifier"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input data-name="title" id="issue-title" placeholder="Insert a title..." type="text" /></h2></td></tr>
			<tr id="changes"><td></td>
				<td>
					<p>Issued by: <a id="issuer"></a></p>
					<p>Last changed: <span id="change-date"></span> by <a id="modifier"></a></p>
				</td>
			</tr>
        </thead>
		<tbody>
			<tr><td colspan="2"><div id="issue-photos"></div></td></tr>
		</tbody>
        <tbody id="issue-table-body">
            <tr><th>Description</th><td><input data-name="description" id="issue-description" type="text" placeholder="Insert a description..."/></td></tr>
			<tr><th>Status</th><td class="status" id="issue-status"></td></tr>
			<tr><th>Priority</th><td><select data-name="priority" id="issue-priority">
				<option class="priority">-- Not Set --</option>
				<option class="priority low" value="low">low</option>
				<option class="priority medium" value="medium">medium</option>
				<option class="priority high" value="high">high</option>
			</select></td></tr>
            <tr><th>Place</th><td><select id="issue-place" data-name="place"></select></tr>
			<tr><th>Location</th><td id="issue-location-link">
				<a href="#" id="show-location-picker">Insert a location...</a>
				<input data-name="latitude" id="issue-latitude" type="hidden" />
				<input data-name="longitude" id="issue-longitude" type="hidden" />
				<span id="hide-location-picker"></span>
			</td></tr>
			<tr><th id="issue-assign-th">Assigned to</th><td>
				<select id="issue-assigned-to">
					<option>-- Nobody --</option>
					<optgroup label="Gruppo" data-name="assigned_group" id="issue-assign-group"></optgroup>
					<optgroup label="Lavoratore" data-name="assigned_worker" id="issue-assign-worker"></optgroup>
				</select>
			</td></tr>
			<!-- Formato umano da leggere -->
			<tr><th>Deadline</th><td><input data-name="due_time" id="issue-due-time" type="date" /></td></tr>
        </tbody>
		<tfoot>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Done" /> <input type="button" id="control-cancel" value="Cancel"/></td></tr>
		</tfoot>
    </table>
	<div class="right-side" id="revisions">
		<h3>Revisions</h3>
		<ul>
			<li><a href="/issue/<?php $this->id; ?>">Current Revision</a></li>
		</ul>
		<div id="location-picker"></div>
    </div>
	<script type="text/javascript" src='http://maps.google.com/maps/api/js?sensor=false&libraries=places'></script>
	<script src="/js/locationpicker.jquery.js"></script>
    <script src="/js/issue/single.js"></script>
</form>