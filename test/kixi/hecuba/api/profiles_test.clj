(ns kixi.hecuba.api.profiles-test
  (:require [kixi.hecuba.api.profiles :refer :all]
           [clojure.test :refer :all]
           [ring.mock.request :refer (body header request content-type)]))

(def csv-profile "id,51b8feea8e4b32fd05171ccc851476d41446274b
entity_id,1245
profile_data_id,22
profile_data_occupancy_under_18,
profile_data_onsite_days_new_build,
profile_data_flat_floor_heat_loss_type,
profile_data_best_u_value_for_walls,
profile_data_estimated_cost_new_build,
profile_data_ter,19.46
profile_data_sap_version_year,October 2005
profile_data_total_envelope_area,
profile_data_sap_regulations_date,
profile_data_multiple_glazing_area_percentage,
profile_data_flat_floor_position,
profile_data_modelling_software_methods_used,
profile_data_co_heating_loss,
profile_data_sap_assessor,\"BSRIA - B661-0002, allan wilson\"
profile_data_inadequate_heating,
profile_data_profile_noise,
profile_data_multiple_glazing_type,Double glazed
profile_data_sap_software,EES SAP 2005.018.03
profile_data_profile_bus_summary_index,
profile_data_thermal_bridging_strategy,
profile_data_sealed_fireplaces,
profile_data_flat_floors_in_block,
profile_data_property_id,8
profile_data_air_tightness_assessor,BSRIA - M Hampson
profile_data_glazing_area_glass_only,Normal
profile_data_final_cost_new_build,
profile_data_lighting_strategy,
profile_data_fabric_energy_efficiency,
profile_data_habitable_rooms,6
profile_data_profile_needs,
profile_data_co_heating_assessor,
profile_data_best_u_value_for_other,
profile_data_renewable_contribution_heat,
profile_data_total_area,
profile_data_profile_temperature_in_summer,
profile_data_draught_proofing_location,
profile_data_heat_storage_present,
profile_data_profile_productivity,
profile_data_number_of_storeys,3
profile_data_passive_solar_strategy,
profile_data_external_perimeter,
profile_data_intervention_completion_date,
profile_data_heat_loss_parameter_hlp,
profile_data_electricity_storage_present,
profile_data_roof_rooms_present,
profile_data_primary_energy_requirement,225.415
profile_data_dwelling_u_value_other,
profile_data_ventilation_approach,
profile_data_construction_time_new_build,
profile_data_draught_proofing,
profile_data_frame_type,uPVC
profile_data_appliances_strategy,
profile_data_bedroom_count,3
profile_data_co_heating_equipment,
profile_data_flat_heat_loss_corridor_other,
profile_data_ber,32.69
profile_data_profile_image_to_visitors,
profile_data_air_tightness_equipment,Fan - Energy Conservatory - Model 4 (230V); Micromanometer - Energy Conservatory - DG700; Barometer - Testo - 511; Thermometer - Testo - 110; Anemometer - Deuta - Anemo
profile_data_innovation_approaches,
profile_data_orientation,
profile_data_total_budget_new_build,
profile_data_best_u_value_for_floors,
profile_data_completeness,50
profile_data_onsite_days,
profile_data_water_saving_strategy,
profile_data_airtightness_and_ventilation_strategy,
profile_data_glazing_area_percentage,
profile_data_occupant_change,Same occupants before and after retrofit
profile_data_intention_ofpassvhaus,
profile_data_profile_health,
profile_data_occupancy_over_60,
profile_data_annual_heating_load,
profile_data_intervention_start_date,
profile_data_profile_design,
profile_data_gross_internal_area,145.42
profile_data_profile_air_in_winter,
profile_data_intervention_description,
profile_data_mains_gas,true
profile_data_profile_lightning,
profile_data_multiple_glazing_type_other,
profile_data_total_volume,443.059
profile_data_sap_version_issue,9.81
profile_data_profile_comfort,
profile_data_heated_habitable_rooms,
profile_data_open_fireplaces,1
profile_data_occupancy_18_to_60,1
profile_data_flat_length_sheltered_wall,
profile_data_planning_considerations,
profile_data_profile_bus_report_url,
profile_data_design_guidance,
profile_data_sap_rating,64.0
profile_data_overheating_cooling_strategy,
profile_data_best_u_value_for_windows,
profile_data_used_passivehaus_principles,
profile_data_moisture_condensation_mould_strategy,
profile_data_ventilation_approach_other,
profile_data_sap_performed_on,2011/02/01
profile_data_best_u_value_for_doors,
profile_data_pipe_lagging,
profile_data_renewable_contribution_elec,
profile_data_controls_strategy,
profile_data_conservation_issues,
profile_data_annual_space_heating_requirement,
profile_data_air_tightness_performed_on,2010/03/17
profile_data_flat_heat_loss_corridor,
profile_data_total_rooms,8
profile_data_space_heating_requirement,14239.5
profile_data_multiple_glazing_u_value,1.7
profile_data_best_u_value_party_walls,
profile_data_best_u_value_for_roof,
profile_data_frame_type_other,
profile_data_electricity_meter_type,Smart
profile_data_category,pre
profile_data_cellar_basement_issues,
profile_data_profile_air_in_summer,
profile_data_co_heating_performed_on,
profile_data_profile_temperature_in_winter,
profile_data_air_tightness_rate,2853.0
profile_data_footprint,74.46
window_sets_0_window_type,Double glazed (unknown install date)
window_sets_0_frame_type,uPVC
window_sets_0_frame_type_other,
window_sets_0_percentage_glazing,
window_sets_0_area,
window_sets_0_location,
window_sets_0_uvalue,1.7
window_sets_0_created_at,2011/11/15 14:53:04 +0000
window_sets_0_updated_at,2011/11/15 14:53:04 +0000
storeys_0_storey_type,Main dwelling
storeys_0_storey,3
storeys_0_heat_loss_w_per_k,
storeys_0_heat_requirement_kwth_per_year,
storeys_0_created_at,2011/11/15 14:51:58 +0000
storeys_0_updated_at,2011/11/15 14:51:58 +0000
walls_0_wall_type,Main dwelling
walls_0_construction,Solid Gold
walls_0_construction_other,
walls_0_insulation,Filled cavity
walls_0_insulation_date,
walls_0_insulation_type,
walls_0_insulation_thickness,
walls_0_insulation_product,
walls_0_uvalue,1.05
walls_0_location,
walls_0_area,
walls_0_created_at,2011/11/15 14:52:23 +0000
walls_0_updated_at,2011/11/15 14:52:23 +0000
heating_systems_0_heating_type,Boiler to radiators or trench heaters
heating_systems_0_heat_source,Gas
heating_systems_0_heat_transport,Water
heating_systems_0_heat_delivery,Convection radiators
heating_systems_0_heat_delivery_source,
heating_systems_0_efficiency_derivation,SEDBUK
heating_systems_0_boiler_type,Combi
heating_systems_0_boiler_type_other,
heating_systems_0_fan_flue,
heating_systems_0_open_flue,
heating_systems_0_fuel,Mains gas 
heating_systems_0_heating_system,Gas boilers: 1998 or later
heating_systems_0_heating_system_other,
heating_systems_0_heating_system_type,Combi with automatic ignition
heating_systems_0_heating_system_type_other,
heating_systems_0_heating_system_solid_fuel,
heating_systems_0_heating_system_solid_fuel_other,
heating_systems_0_bed_index,
heating_systems_0_make_and_model,\"Worcester, 28i Junior\"
heating_systems_0_controls,Programmer room thermostat and TRVs
heating_systems_0_controls_other,
heating_systems_0_controls_make_and_model,
heating_systems_0_emitter,Radiators
heating_systems_0_trvs_on_emitters,
heating_systems_0_use_hours_per_week,
heating_systems_0_installer,
heating_systems_0_installer_engineers_name,
heating_systems_0_installer_registration_number,
heating_systems_0_commissioning_date,
heating_systems_0_inspector,
heating_systems_0_inspector_engineers_name,
heating_systems_0_inspector_registration_number,
heating_systems_0_inspection_date,
heating_systems_0_created_at,2011/11/15 14:49:38 +0000
heating_systems_0_updated_at,2011/11/15 14:49:38 +0000
heating_systems_0_efficiency,78.9
hot_water_systems_0_dhw_type,From main
hot_water_systems_0_fuel,Mains gas
hot_water_systems_0_fuel_other,
hot_water_systems_0_immersion,
hot_water_systems_0_cylinder_capacity,No cylinder
hot_water_systems_0_cylinder_capacity_other,
hot_water_systems_0_cylinder_insulation_type,
hot_water_systems_0_cylinder_insulation_type_other,
hot_water_systems_0_cylinder_insulation_thickness,
hot_water_systems_0_cylinder_insulation_thickness_other,
hot_water_systems_0_cylinder_thermostat,No cylinder
hot_water_systems_0_controls_same_for_all_zones,true
hot_water_systems_0_created_at,2011/11/15 14:51:35 +0000
hot_water_systems_0_updated_at,2011/11/15 14:51:35 +0000
low_energy_lights_0_light_type,Other (please specify)
low_energy_lights_0_light_type_other,10 L.E.L
low_energy_lights_0_bed_index,
low_energy_lights_0_created_at,2011/11/15 14:54:04 +0000
low_energy_lights_0_updated_at,2011/11/15 14:54:04 +0000
low_energy_lights_0_proportion,34.0
timestamp,2011-11-17 09:40:48+0000")

(def bad-profile {"bad" "data"})
(def simple-schema [:id
                    {:name :nested_item
                     :type :nested-item
                     :schema [:nested_id]}
                    {:name :association
                     :type :associated-items
                     :schema [:associated_id]}])

(deftest parse-by-schema-test
  (testing "bad input, return an empty schema"
    (is (= {:id nil, :nested_item {:nested_id nil}, :association ()}
           (parse-by-schema bad-profile simple-schema))))
  (testing "input with valid simple attribute"
    (is (= {:id "test", :nested_item {:nested_id nil}, :association ()}
           (parse-by-schema {"id" "test"} simple-schema))))
  (testing "input with valid nested item"
    (is (= {:id nil, :nested_item {:nested_id "test"}, :association ()}
           (parse-by-schema { "nested_item_nested_id" "test" } simple-schema))))
  (testing "input with valid associated items"
    (is (= {:id nil
            :nested_item {:nested_id nil}
            :association [{:associated_id "test"} {:associated_id "test2"}]})
           (parse-by-schema {"association_0_associated_id" "test"
                             "association_1_associated_id" "test2" }
                            simple-schema))))

