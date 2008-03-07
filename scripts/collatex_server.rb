#!/bin/env ruby
# script for starting/stopping the CollateX webserver

require 'ftools'

PROGRAM_CLASS = 'com.sd_editions.collatex.Web.CollateXServer'
PROGRAM = 'CollateXServer'
LOG_DIR = '/var/log/CollateX'

def get_pid
	`ps -ef|grep #{PROGRAM_CLASS}|grep java|grep -v grep|cut -c10-16`
end

def program_is_running?
	!get_pid.empty?
end

def get_jars
  Dir.glob(File.join("lib","*.jar"))
end

def check_setup
  if !(File.exist?(LOG_DIR) && File.directory?(LOG_DIR))
    File.makedirs(LOG_DIR)
  end
end

def start
	if program_is_running?
		puts "#{PROGRAM} is already running, try #{__FILE__} restart"
		return
	end
  print "starting #{PROGRAM}... "
  classpath=get_jars.join(':')
  command="java -cp #{classpath} #{PROGRAM_CLASS} >> #{LOG_DIR}/#{PROGRAM}.log 2>> #{LOG_DIR}/#{PROGRAM}.err &"
  `#{command}`
  if !program_is_running?
  	puts "oops! Something seems to have gone wrong, check the logs in #{LOG_DIR}"
  else
    puts "done!"
  end
end

def stop
  print "stopping #{PROGRAM}... "
  if program_is_running?
    `kill #{get_pid}`
    sleep(2)
    
    puts "done!"
  else
    puts "#{PROGRAM_CLASS} was not running."
  end
end

def status
	puts program_is_running? ? "#{PROGRAM} is running" : "#{PROGRAM} is not running" 
end

#main

check_setup
case ARGV[0]
  when 'start'
    start
  when 'stop'
    stop
  when 'restart'
    stop
    start
  when 'status'
  	status
  else
    puts "usage: #{__FILE__} {start|stop|restart|status}"
end