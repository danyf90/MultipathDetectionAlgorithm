<?php

namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

class PlaceController implements IController {
    public static function handle(Template $t, array $requestParams) {
		
		if (count($requestParams) > 0) {
			$id = array_shift($requestParams);
			$requestedTemplate = "templates/template-place.php";
		 	$placeTemplate = new Template($requestedTemplate);
			$placeTemplate->id = $id;
		} else {
			$requestedTemplate = "templates/template-place-all.php";
			$placeTemplate = new Template($requestedTemplate);
		}
        
        $t->content = $placeTemplate;
    }
}

?>