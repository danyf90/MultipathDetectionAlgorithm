<?php

namespace Vineyard\Model;

use \Vineyard\Utility\IResource;
use \Vineyard\Utility\TGetSet;
use \JsonSerializable;

class Issue implements IResource, JsonSerializable {
    
    use TGetSet; // uses a trait, it defines accessors (standard getter and setter) and json serialization of properties
    
    public static function handleRequest($method, array $requestParameters) {
        
        if (!empty($requestParameters)) { // api/issue/<id>
            
            if ($method == 'GET') {

                // Issue example
                $issue = new self;
                $issue->id = array_shift($requestParameters);
                $issue->assignee = 28;
                $issue->create_time = time();
                $issue->assign_time = strtotime("+1 day");
                $issue->due_time = strtotime("+1 week");
                $issue->status = 'assigned';
                $issue->priority = 'medium';
                $issue->issuer = 13;
                $issue->place = 4;
                $issue->title = "Rifare innesto qui e qua";
                $issue->description = "Così fa cagare";
                $issue->location = [52.593800, 21.448850];
                $issue->assigned_worker = 13;
                $issue->assigned_group = null;

                // json-serialize it!
                return json_encode($issue);
            }
        }
    }
}

?>