#!/usr/bin/env ruby

require 'rubygems'
require 'sequel'

#warehouseid and times from user

puts "Enter warehouse_id for which inventory has to be deleted"
warehouse_id = gets.chomp()
# $i=0;
# while($i<$t)
#   puts"hyee"
#   $i+=1;
# end

db_connection = Sequel.connect(:adapter => 'mysql2', :username => 'root', :host => 'localhost', :database => 'Dummy')

#insert = db_connection.query("INSERT INTO inventory_items(storage_location_id) VALUES(#{ids[0]})")


#Check for storage_zone is present for that warehouse_id
storage_zones_table_name="storage_zones"
storage_zones_id= db_connection.from(storage_zones_table_name).select(:id).where(:warehouse_id => warehouse_id , :name => 'perf_storage_zone' ).all
storage_zones_ids=storage_zones_id.collect{|x| x[:id]}
storage_zones_id= db_connection.from(storage_zones_table_name).where(:warehouse_id => warehouse_id , :name => 'perf_storage_zone' ).delete
# p "========"
#  p storage_zones_id
# p "========="

p storage_zones_ids

#Check for picking_zone is present for that warehouse_id
picking_zones_table_name="picking_zones"
picking_zones_id= db_connection.from(picking_zones_table_name).select(:id).where(:warehouse_id => warehouse_id , :name => 'perf_picking_zone' ).all
picking_zones_ids=picking_zones_id.collect{|x| x[:id]}
picking_zones_id= db_connection.from(picking_zones_table_name).where(:warehouse_id => warehouse_id , :name => 'perf_picking_zone' ).delete
# p "========"
#  p picking_zones_id
# p "========="

p picking_zones_ids


#Check for storage_locations present for that warehouse_id

storage_locations_table_name = "storage_locations"
storage_locations_id = db_connection.from(storage_locations_table_name ).select(:id).where(:warehouse_id => warehouse_id , :picking_zone_id => picking_zones_ids[0] , :storage_zone_id => storage_zones_ids[0]).all
storage_locations_ids = storage_locations_id.collect{|x| x[:id]}
storage_locations_id= db_connection.from(storage_locations_table_name).where(:warehouse_id => warehouse_id , :picking_zone_id => picking_zones_ids[0] , :storage_zone_id => storage_zones_ids[0]).delete

p storage_locations_ids

#Create inventory_items for that warehouse_id
inventory_items_table_name = "inventory_items"
storage_locations_ids.each do |x|
  db_connection.from(inventory_items_table_name).
      where(:storage_location_id => x , :warehouse_id => warehouse_id, :wid =>"A10009", :updated_by => "perf-user").delete
end

p "done"