<?php

namespace Vineyard\Model;

use \Vineyard\Utility\IResource;

use \Vineyard\Model\AbstractTask;


class Task extends AbstractTask implements IResource {
	
	static public function isIssueInstance() { return false; }
	
    public function check() {
        $v = parent::check();
        // issuer
        $v->notSet('issuer');

        return $v->getWrongFields();
    }

    // Override AbstractORM::onPostInsert()
    protected function onPostInsert() {
		parent::onPostInsert();

		// Send notifications
		$this->notify("task-insertion");
	}

    static public function getById($id) { return parent::getById($id, false); }
	
	protected function onPostUpdate() {
		if ($this->status == "resolved")
			$this->notify("task-resolved");
		else $this->notify("task-modification");

		parent::onPostUpdate();
	}
}

?>
