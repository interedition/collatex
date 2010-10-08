#!/usr/bin/env ruby
require 'rubygems'
require 'mongrel'
require 'logger'
require 'pp'

LISTING_ALLOWED = true

class SVGHandler < Mongrel::HttpHandler

  def process(request, response)
    #pp request
    #pp response
    cgi = Mongrel::CGIWrapper.new(request, response)
    #pp cgi
    dot = cgi['dot']
    #pp dot

    input='input.dot'
    File.open(input,'w'){|f| f.puts dot}
    svg = `dot -Grankdir=LR -Gid=VariantGraph -Tsvg #{input}`
    #pp svg

    response.start(200,true) do |head,out|
      head['Content-Type'] = 'image/svg+xml'
      out << svg
    end
  end

end

class Server
  attr_reader :mongrel_server

  def is_windows?
    RUBY_PLATFORM =~ /(win|w)32$/
  end

  def initialize(port="80")
    @port=port
  end

  def start
    $log.info("Starting server at port #{@port}")
    print "Starting server at port #{@port}..."
    @mongrel_server = Mongrel::HttpServer.new("0.0.0.0", @port)
    mongrel_server.register("/svg", SVGHandler.new)
    trap('INT') { shutdown }
    trap('TERM'){ shutdown }
    server_thread = mongrel_server.run
    puts 'done! (Ctrl-c to stop)'
    sleep
  end

  def shutdown
    print "Gracefully shutting down the server..."
    $log.info "Gracefully shutting down the server..."
    mongrel_server.graceful_shutdown
    puts "done!"
    exit
  end
end

# main
if __FILE__ == $0
  $log=Logger.new('server.log','monthly')
  $log.datetime_format = "%Y-%m-%d %H:%M:%S"
  Server.new(1080).start
  $log.close
end
