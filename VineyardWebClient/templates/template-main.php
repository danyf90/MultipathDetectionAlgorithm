<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title><?php $this->title = "Vineyard Web Client"; ?></title>
        <link href="/style.css" rel="stylesheet" type="text/css" />
        <script src="/js/jquery.min.js"></script>
        <script src="/js/vineyard.js"></script>
    </head>
    <body>
        <header>
            <h1><a href="/admin/">Vineyard</a></h1>
            <ul>
                <li>Status Bar</li>
                <li>Data/Ora</li>
                <li>Issue/Task indicator</li>
                <li><a href="#">Logout</a></li>
            </ul>
        </header>
        <main>
            <nav>
                <ul>
                    <li class="menu-item-place"><a href="/place/"><span>PLACES</span></a></li>
                    <li class="menu-item-task"><a href="/task/"><span>TASKS</span></a></li>
                    <li class="menu-item-issue"><a href="/issue/"><span>ISSUES</span></a></li>
                    <li class="menu-item-worker"><a href="/worker/"><span>WORKERS</span></a></li>
                    <li class="menu-item-group"><a href="/group/"><span>GROUPS</span></a></li>
                </ul>
            </nav>
            <?php $this->content = '<div id="content"><p>Mmm.. this page seems empty.</p></div>'; ?>
        </main>
        <footer>
            <p>Vineyard Web Client. All rights reserved &copy; TrainSoft Inc.</p>
        </footer>
    </body>
</html>
