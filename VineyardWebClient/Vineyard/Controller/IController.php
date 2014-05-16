<?php
namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

interface IController {
    public static function handle(Template $t, array $requestParams);
}
?>