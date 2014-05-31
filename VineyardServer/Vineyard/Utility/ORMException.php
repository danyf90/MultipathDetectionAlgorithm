<?php
namespace Vineyard\Utility;

class ORMException extends \Exception {

    protected $wrongFields = null;

    public function __construct($message, $code) {
        parent::__construct($message, $code);
    }

    public function setWrongFields(array $wf) {
        $this->wrongFields = $wf;
    }

    public function getWrongFields() {
        return $this->wrongFields;
    }
};

?>
