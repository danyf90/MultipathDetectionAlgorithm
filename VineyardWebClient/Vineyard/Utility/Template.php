<?php

namespace Vineyard\Utility;

/**
 * Helper class for Template.
 */
class TemplateContext {
        
    protected $_data = array();

    public function __get($name) {
        if (isset($this->_data[$name]))
            echo (string) $this->_data[$name];
    }

    public function __set($name, $value) {
        if (isset($this->_data[$name]))
            echo (string) $this->_data[$name];
        else {
            $this->set($name, $value);
            echo (string) $value;
        }
    }
    
    public function get($name) {
        if (isset($this->_data[$name]))
            return $this->_data[$name];
    }
    
    public function set($name, $value) {
        $this->_data[$name] = $value;
    }
    
    public function render($templateFile) {
        ob_start();
		include($templateFile);
		ob_end_flush();
    }
}


class Template {
    
    protected $templateFile;
    
    protected $context;
    
    public function __construct($file) {
        if (!file_exists($file))
            throw new \Exception("File not found: `" + $file + "`");
        
        $this->templateFile = $file;
        $this->context = new TemplateContext();
    }
    
    public function __get($name) {
        return $this->context->get($name);
    }
    
    public function __set($name, $value) {
        $this->context->set($name, $value);
    }
    
    public function render()
	{
        $this->context->render($this->templateFile);
	}
    
    public function __toString()
    {
        ob_start();
        $this->render();
        return ob_get_clean();
    }
}
?>