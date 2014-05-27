<form class="content entity" id="place">
	<input type="hidden" id="place-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading"><span>Loading...</span></th><td><h2><input data-name="name" id="place-name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="place-table-body">
            <tr><th>Description</th><td><input data-name="description" id="place-description" type="text" placeholder="Insert a description..."/></td></tr>
            <tr><th>Location</th><td id="place-location-link">
				<a href="#" id="show-location-picker">Insert a location...</a>
				<input data-name="latitude" id="place-latitude" type="hidden" />
				<input data-name="longitude" id="place-longitude" type="hidden" />
				<span id="hide-location-picker"></span>
			</td></tr>
			<tr><th>Parent</th><td id="place-parent"><select data-name="parent"><option>-- Nessuno --</option></select></td></tr>
        </tbody>
		<tbody id="place-attributes">
		</tbody>
		<tfoot>
			<tr><th></th><td class="empty"><a href="#" id="place-add-attribute">+ Add an attribute</a></td></tr>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Done" /> <input type="button" id="control-cancel" value="Cancel"/></td></tr>
		</tfoot>
    </table>
    <div class="right-side" id="place-photo">
        <a class="add" href="#"></a>
        <a class="delete" href="#"></a>
		<div class="hidden"><input type="file" id="new-photo" /></div>
		<progress></progress>
		<div id="location-picker"></div>
    </div>
	<script type="text/javascript" src='http://maps.google.com/maps/api/js?sensor=false&libraries=places'></script>
	<script src="/js/locationpicker.jquery.js"></script>
    <script src="/js/place/single.js"></script>
</form>