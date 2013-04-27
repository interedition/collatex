<?php

require "vendor/autoload.php";

// -------------------------------------------------------------------------------- Template System Setup

function markdown($input)
{
    $markdownParser = new \dflydev\markdown\MarkdownParser();
    return $markdownParser->transformMarkdown($input);
}

\Twig_Autoloader::register();

$twig = new \Twig_Environment(new \Twig_Loader_Filesystem("twig"));
$twig->addGlobal("server", $_SERVER);
$twig->addGlobal("version", "1.3-SNAPSHOT");
$twig->addFilter("markdown", new \Twig_Filter_Function('markdown', array('is_safe' => array('html'))));

class TwigView extends \Slim\View
{
    public function render($template)
    {
        global $twig;
        return $twig->loadTemplate($template)->render($this->data);
    }
}

// -------------------------------------------------------------------------------- Application Setup

$app = new \Slim\Slim(array('templates.path' => "twig", 'view' => new TwigView()));

$app->get("/", function() use ($app) {
    $app->render("index.twig");
});
$app->get("/about/", function() use ($app) {
    $app->render("project.twig", array("title" => "About the Project"));
});
$app->get("/doc/", function() use ($app) {
    $app->render("doc.twig", array("title" => "Documentation"));
});
$app->get("/download/", function() use ($app) {
    $app->render("download.twig", array("title" => "Download"));
});
$app->run();

?>