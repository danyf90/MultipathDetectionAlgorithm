<?php

namespace Vineyard\Utility;

interface IResource {
    public static function handleRequest($method, array $requestParameters);
}

?>
