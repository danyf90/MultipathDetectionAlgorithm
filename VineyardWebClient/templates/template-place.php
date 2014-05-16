<form class="content" id="place">
	<input type="hidden" name="id" id="place-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading"><img src="/images/loading.gif" />Loading...</th><td><h2><input id="place-name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="place-table-body">
            <tr><th>Descrizione</th><td><input id="place-description" type="text" placeholder="Insert a description..."/></td></tr>
            <tr><th>Posizione</th><td id="place-location-link"></td></tr>
        </tbody>
		<tfoot id="place-add-attribute">
			<tr><th></th><td class="empty"><a href="#">+ Aggiungi un attributo</a></td></tr>
		</tfoot>
    </table>
    <div id="place-photo">
        <a class="add" href="#"></a>
        <a class="delete" href="#"></a>
    </div>
    <script src="/js/place/single.js"></script>
</form>