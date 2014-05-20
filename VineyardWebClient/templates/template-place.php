<form class="content entity" id="place">
	<input type="hidden" id="place-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading"><img src="/images/loading.gif" /><span>Loading...</span></th><td><h2><input data-name="name" id="place-name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="place-table-body">
            <tr><th>Descrizione</th><td><input data-name="description" id="place-description" type="text" placeholder="Insert a description..."/></td></tr>
            <tr><th>Posizione</th><td id="place-location-link"><a href="#">Inserisci un punto geografico...</a></td></tr>
			<tr><th>Parent</th><td id="place-parent"><select data-name="parent"><option>-- Nessuno --</option></select></td></tr>
        </tbody>
		<tbody id="place-attributes">
		</tbody>
		<tfoot>
			<tr><th></th><td class="empty"><a href="#" id="place-add-attribute">+ Aggiungi un attributo</a></td></tr>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Fatto" /> <input type="button" id="control-cancel" value="Annulla"/></td></tr>
		</tfoot>
    </table>
    <div id="place-photo">
        <a class="add" href="#"></a>
        <a class="delete" href="#"></a>
    </div>
    <script src="/js/place/single.js"></script>
</form>