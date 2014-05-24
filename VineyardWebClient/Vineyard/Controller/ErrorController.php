<?php

namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

class ErrorController implements IController {
    public static function handle(Template $t, array $requestParams) {
        
        $requestedTemplate = "templates/template-error.php";
        
        $errorTemplate = new Template($requestedTemplate);
        $errorTemplate->errorCode = http_response_code();
		
        $t->content = $errorTemplate;
		return $t;
    }
}

?>