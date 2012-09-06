#!/usr/bin/perl
use strict;
use warnings;

use Treex::Core;
use Treex::Block::CzsemRpcReader;
use RPC::XML::Server; 
use RPC::XML; 
use Net::Address::IP::Local;
use Sys::Hostname;
use Treex::Core::Log;

sub debugPrint {
#  print STDERR @_;
}

debugPrint "treex online start\n";


Treex::Core::Log::log_set_error_level('INFO');


my $port_number = 9090;
my $isReady = 0;
my $scenario = undef;

my @scenarioSetup = (
  'Util::SetGlobal language=cs',
  'CzsemRpcReader',
  'W2A::CS::Segment',
  'devel\analysis\cs\s_w2t.scen');


$RPC::XML::ENCODING = "UTF-8";


sub startServer
{
  my $srv = RPC::XML::Server->new(port => $port_number); #server object
  
  $srv->add_method(
   {
    "name"      => "treex.initScenario", 
    "signature" => ['string string array'], 
    "code"      => \&initScenarioSrv
   }
  );

  $srv->add_method(
   {
    "name"      => "treex.analyzeText", 
    "signature" => ['array string'], 
    "code"      => \&analyzeTextSrv
   }
  );

  $srv->add_method(
   {
    "name"      => "treex.encodeDoc", 
    "signature" => ['array string'], 
    "code"      => \&encodeDocSrv
   }
  );
  
  $srv->add_method(
   {
    "name"      => "treex.terminate", # calling this function will throw fault aka exception
    "signature" => ['string'], #what are the return type and call parameters beware of the spaces!
    "code"      => \&terminate
   }
  );
  
  my $host = hostname;  
  print "Starting Treex RPC server at http://$host:$port_number ( http://" . Net::Address::IP::Local->public . ":$port_number )\n";
  
  $srv->server_loop; # Just work
}


sub isReady
{
   return eval $isReady;
}

sub terminate
{
    exit;
}

sub initScenario
{
  my $lang = shift;
  my $blocks = shift;
  my $scenStr = "Util::SetGlobal language=$lang CzsemRpcReader " . join(' ', @$blocks);  
  $scenario = Treex::Core::Scenario->new(from_string => $scenStr);
}


sub analyzeText
{
  my $text = shift;
  my $doc = Treex::Core::Document->new;
  
  my $docParams = {
     doc => $doc,
     text => $text
  };
 	
	$Treex::Block::CzsemRpcReader::dataQueue->enqueue($docParams);  
  $Treex::Block::CzsemRpcReader::dataQueue->enqueue(undef);  
  $scenario->run;
  
  # DEBUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	#$doc->save('C:\workspace\demo_cz.treex');
  # DEBUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!
  
  return encodeLoadedDoc($doc);
}


sub processNode
{
  my $node  = shift;
  my $ret = {};
  
  if ($node->parent) {
    debugPrint  "parent: ";
    debugPrint  $node->parent->id;
    $ret->{"parent_id"} = $node->parent->id;
  } 
  
  debugPrint  "\ntype: ";
  debugPrint  $node->get_pml_type_name . "\n";
  $ret->{"pml_type_name"} = $node->get_pml_type_name; 

  
  foreach my $path ( $node->attribute_paths ) {
    my $value = $node->attr($path);
    my $valueAll = $node->all($path);
    
    if ($value) {
      if (UNIVERSAL::isa($value,'ARRAY'))
      {
        my @arr_value = $value->values;
        my $finall_value = [];
        debugPrint  "$path = @arr_value ($valueAll)\n";
        foreach my $v (@arr_value)
        {
          push(@$finall_value, $v);
        }
        
        $ret->{$path} = $finall_value;           
      } else {
        debugPrint  "$path = $value ($valueAll)\n";
        utf8::encode($value); 
        $ret->{$path} = $value; 
      } 
    } 
  }  
  return $ret;
}

sub encodeDoc
{
  my $filename = shift;
  my $doc = Treex::Core::Document->new( { filename => $filename } ); 
  return encodeLoadedDoc($doc); 
}

sub encodeLoadedDoc
{
  my $doc = shift;  
  my $zones = [];

  foreach my $bundle ( $doc->get_bundles ) {
    foreach my $bundlezone ( $bundle->get_all_zones ) {
      my $nodes = [];
      my $roots = [];
      my $sentence = $bundlezone->sentence;
      my $language = $bundlezone->language; 
      my $selector = $bundlezone->selector;
      
      utf8::encode($sentence); 
 
      debugPrint  "-----------sentence--------\n";      
      debugPrint  $bundlezone->sentence . "\n";      
      foreach my $root ( $bundlezone->get_all_trees ) {
        debugPrint  "--root--\n";
        debugPrint  $root->id;
        debugPrint  "\n";
        push(@$roots, processNode($root));          
        foreach my $node ( $root->get_descendants({}) ) {
          debugPrint  "----node------\n";
          
          push(@$nodes, processNode($node));          
        }
      }
      my $zone = {         
        language    => $language, 
        selector    => $selector, 
        roots       => $roots,
        nodes       => $nodes,
        sentence    => $sentence };
      
      push (@$zones, $zone);
    }
  }
  	
  return $zones;
}


sub analyzeTextSrv
{
  shift; #server context    
  return analyzeText(@_); 
}

sub encodeDocSrv
{
  shift; #server context
  return encodeDoc(@_);
}

sub initScenarioSrv
{
  shift; #server context
  return initScenario(@_);
}

 
#initScenario(@scenarioSetup);

startServer;


