<?php

namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

class HomeController implements IController {
    public static function handle(Template $t, array $requestParams) {
        
		$t->content = '<div style="padding: 40px;">';
        $t->content .= '<h2>Welcome to Vineyard Web Client!</h2>';
		$t->content .= '<p>This page is currently empty, choose something in the menu on the left.</p>';
		$t->content .= '</div>';
    }
}

?>