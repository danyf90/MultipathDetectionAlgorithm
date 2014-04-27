<?php
namespace ADM\Exceptions;

class ORMException extends \Exception {

	protected $wrongFields = null;
	
	public function setWrongFields(array $wf) {
		$this->wrongFields = $wf;
	}
	
	public function getWrongFields() {
		return $this->wrongFields;
	}
};

?>
