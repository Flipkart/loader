#!/usr/bin/env ruby

require 'rubygems'
require 'sequel'

#warehouseid and times from user

puts "Enter warehouse_id for which inventory has to be created"
warehouse_id = gets.chomp()
puts"Enter the number of times you want to create the inventory"
t=gets.to_i
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

if(storage_zones_id.size()==0)
 #insert in the table
  db_connection.from(storage_zones_table_name).
      insert(:name => "perf_storage_zone" ,:warehouse_id=> warehouse_id,:area => "store", :created_at => Time.now, :updated_at => Time.now)
  storage_zones_id= db_connection.from(storage_zones_table_name).select(:id).where(:warehouse_id => warehouse_id , :name => 'perf_storage_zone' ).all
end
storage_zones_ids=storage_zones_id.collect{|x| x[:id]}
# p "========"
#  p storage_zones_id
# p "========="

p storage_zones_ids

#Check for picking_zone is present for that warehouse_id
picking_zones_table_name="picking_zones"
picking_zones_id= db_connection.from(picking_zones_table_name).select(:id).where(:warehouse_id => warehouse_id , :name => 'perf_picking_zone' ).all
if(picking_zones_id.size()==0)
  #insert in the table
  db_connection.from(picking_zones_table_name).
      insert(:name => "perf_picking_zone" ,:warehouse_id=> warehouse_id,:area => "store", :created_at => Time.now, :updated_at => Time.now, :is_cross_zone => 1, :max_items_in_a_picklist => 12, :picking_algorithm => "location_sequence", :floor_no => 1)
  picking_zones_id= db_connection.from(picking_zones_table_name).select(:id).where(:warehouse_id => warehouse_id , :name => 'perf_picking_zone' ).all
end
picking_zones_ids=picking_zones_id.collect{|x| x[:id]}
# p "========"
#  p picking_zones_id
# p "========="

p picking_zones_ids


#Check for storage_locations present for that warehouse_id

storage_locations_table_name = "storage_locations"
storage_locations_id = db_connection.from(storage_locations_table_name ).select(:id).where(:warehouse_id => warehouse_id , :picking_zone_id => picking_zones_ids[0] , :storage_zone_id => storage_zones_ids[0]).all
if(storage_locations_id.size()==0)
  #insert in the table
  db_connection.from(storage_locations_table_name).
      insert(:location_type => "store" ,:label=> "store-shelf",:warehouse_id => warehouse_id, :capacity =>15, :created_at => Time.now, :updated_at => Time.now, :picking_zone_id => picking_zones_ids[0], :storage_zone_id =>storage_zones_ids[0], :available_capacity => 15, :lock_version => 0, :location_sequence => 0, :not_pickable => 0, :seller_id => "fki", :is_deleted => 0, :type => "InventoryModule::StorageLocation")
  storage_locations_id = db_connection.from(storage_locations_table_name ).select(:id).where(:warehouse_id => warehouse_id , :picking_zone_id => picking_zones_ids[0] , :storage_zone_id => storage_zones_ids[0]).all
end
storage_locations_ids = storage_locations_id.collect{|x| x[:id]}

p storage_locations_ids

#Create inventory_items for that warehouse_id
inventory_items_table_name = "inventory_items"
t.times do
 db_connection.from(inventory_items_table_name).
   insert(:storage_location_id => storage_locations_ids.sample ,:atp=> 1,:quantity => 0, :created_at => Time.now, :updated_at => Time.now, :warehouse_id => warehouse_id, :wid =>"A10009", :updated_by => "perf-user", :lock_version => 0, :in_transit => 0)
end

#{10.times do
#  if(i==ids.size)
#    i=0;
#  end
#  db_connection.from("inventory_items").
#    insert(:storage_location_id => ids[i] ,:atp=> 1,:quantity => 0, :created_at => Time.now, :updated_at => Time.now, :warehouse_id => "blr", :wid =>"A10009", :updated_by => "perf-user", :lock_version => 0, :in_transit => 0)
#    i+=1
#end}
p "done"