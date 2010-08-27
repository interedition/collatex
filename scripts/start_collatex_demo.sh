# start the relevant servers for collatex demo

function start {
  name=$1
  startdir=$2
  cmd=$3
  echo "Starting $name"
  date +"%Y-%m-%d %k:%M" >>~/log/$name.log
  date +"%Y-%m-%d %k:%M" >>~/log/$name.err
  cd $startdir && nohup sh -c "exec nice $cmd 1>> ~/log/$name.log 2>>~/log/$name.err" >/dev/null &
  echo $! >~/log/$name.pid
}

mkdir ~/log 2>/dev/null
basedir="/home/bramb/1.0development-bramb"
start "svgserver"       "$basedir/collatex-web/src/main/ruby" "ruby svgserver.rb"
start "collatex-web"    "$basedir/collatex-web"               "mvn jetty:run"
start "collatex-client" "$basedir/collatex-clients"           "mvn -Djetty.port=2001 jetty:run"
