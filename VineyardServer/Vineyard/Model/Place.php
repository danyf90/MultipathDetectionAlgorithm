<?php

namespace Vineyard\Model;

use \Vineyard\Utility\IResource;
use \Vineyard\Utility\TGetSet;
use \JsonSerializable;

class Place implements IResource, JsonSerializable {
    
    use TGetSet; // uses a trait, it defines accessors (standard getter and setter) and json serialization of properties
    
    public static function handleRequest($method, array $requestParameters) {
        
        if (empty($requestParameters)) { // api/place/
        
            if ($method == 'GET') { // places list requested
                // Place list example
                $place = new self;
                $place->id = 4;
                $place->parent = null;
                $place->name = "Vigna A";
                $place->description = "La prima vigna, piantata di traverso";
                $place->location = [52.593800, 21.448850];

                $place2 = new self;
                $place2->id = 6;
                $place2->parent = null;
                $place2->name = "Vigna B";
                $place2->description = "La seconda vigna, piantata di sghimbescio";
                $place2->location = [21.448850, 52.593800];

                $place3 = new self;
                $place3->id = 8;
                $place3->parent = 6;
                $place3->name = "Filare B.1";
                $place3->description = "Il primo filare della vigna B";
                $place3->location = [21.448850, 52.593800];

                // json-serialize it!
                return json_encode([$place, $place2, $place3]);
            }
            
            if ($method == 'POST') { // new place insertion requested
                // ...
            }
            
            
        } else { // api/place/<id>
            
            if ($method == "GET") {
                // particular place example
                $place = new self;
                $place->id = array_shift($requestParameters);
                $place->parent = null;
                $place->name = "Vigna A";
                $place->description = "La prima vigna, piantata di traverso";
                $place->location = [52.593800, 21.448850];

                return json_encode($place);
            }
        }
    }
}

?>