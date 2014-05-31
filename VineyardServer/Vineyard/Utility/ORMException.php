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

    public function getJSONMessage() {
	if (!is_null($this->wrongFields))
	    return $this->wrongFields();
	return $this->getMessage();
    }

    public function getWrongFields() {
        return $this->wrongFields;
    }
};

?>
