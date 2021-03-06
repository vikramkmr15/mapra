package metabotogeno.node;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;

import metabotogeno.algo.MetaboToGenoDriver;
import nodeutils.ColumnSpecification;
import nodeutils.TableFunctions;
import togeno.ScoredGene;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * This is the model implementation of MetaboToGeno.
 * 
 *
 * @author Marie-Sophie Friedl
 */
public class MetaboToGenoNodeModel extends NodeModel {
    
    /** logger of the node for writing to KNIME log file and console*/
    private static final NodeLogger logger = NodeLogger.getLogger(MetaboToGenoNodeModel.class);
    
    /** key of the option annotation mode */
    static final String CFGKEY_ANNOTATION_MODE = "mode_of_annotation";
    /** string value coding for annotation mode using only the maximum score */
    static final String ANNOTATION_MODE_MAX="annoMax";
    /** string value coding for annotation mode combining all scores */
    static final String ANNOTATION_MODE_MULTIPLE="annoMultiple";
    /** default value of the option annotation mode */
    static final String DEFAULT_ANNOTATION_MODE=ANNOTATION_MODE_MULTIPLE;
    private final SettingsModelString m_modeAnno = new SettingsModelString(
    		CFGKEY_ANNOTATION_MODE, DEFAULT_ANNOTATION_MODE);    
    
    /** inPort number for table with metabolite scores */
    private static final int INPORT_SCOREMETABOLITES=0;
    /** inPort number for table with metabolite - gene associations */
    private static final int INPORT_METABOLITE_GENE=1;
    /** inPort number for table with all gene ids*/
    private static final int INPORT_ALL_GENES=2;

    /**
     * Constructor for the node model.
     * The node has 3 incoming ports.
     * Port 0: result of ScoreMetablites, Port 1: metabolite-gene associations, Port 2: list of all genes
     */
    protected MetaboToGenoNodeModel() {
        // 3 incoming ports, 1 outgoing port
        super(3, 1);
    }

    /**
     * method to execute MetaboToGeno
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	//read list of all genes from KNIME table
    	LinkedList<String> allGenes=TableProcessorMetaboToGeno.getGeneList(inData[INPORT_ALL_GENES], logger);
    	//read associations between metabolites and genes from KNIME table
    	HashMap<String, LinkedList<String>> associations=TableProcessorMetaboToGeno.getAssociationData(
    			inData[INPORT_METABOLITE_GENE], logger, allGenes);
    	//read scores from ScoreMetabolites
    	LinkedList<String[]> metaboScores = TableProcessorMetaboToGeno.getScoreMetabolitesResult(
    			inData[INPORT_SCOREMETABOLITES], logger, associations);
  
    	//configure driver -> select annotation mode
    	MetaboToGenoDriver driver = new MetaboToGenoDriver(allGenes, associations, metaboScores);
    	if(m_modeAnno.getStringValue().equals(ANNOTATION_MODE_MULTIPLE)){
    		driver.setModeOfAnnotation(true);
    	}
    	else{
    		driver.setModeOfAnnotation(false);
    	}
    	
    	//execute algorithm
    	LinkedList<ScoredGene> result = driver.runMetaboToGeno();
    	
    	//generate output
    	BufferedDataTable out = TableProcessorMetaboToGeno.generateMTGOutputTable(exec, result, 
    			inData[INPORT_SCOREMETABOLITES]);
    	
    	return new BufferedDataTable[]{out};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
    	//check table with metabolite scores for 2 columns: metabolite id and pvalue
    	TableFunctions.checkColumn(inSpecs, INPORT_SCOREMETABOLITES, ColumnSpecification.METABOLITE_ID,
    			ColumnSpecification.METABOLITE_ID_TYPE, null);
    	TableFunctions.checkColumn(inSpecs, INPORT_SCOREMETABOLITES, ColumnSpecification.METABOLITE_SIGNIFICANCE,
    			ColumnSpecification.METABOLITE_SIGNIFICANCE_TYPE, null);
    	
    	//check table with metabolite - gene associations for columns 
    	TableFunctions.checkColumn(inSpecs, INPORT_METABOLITE_GENE, ColumnSpecification.METABOLITE_ID,
    			ColumnSpecification.METABOLITE_ID_TYPE, null);
    	TableFunctions.checkColumn(inSpecs, INPORT_METABOLITE_GENE, ColumnSpecification.GENE_ID,
    			ColumnSpecification.GENE_ID_TYPE, null);
    	
    	//check table with list of all genes
    	TableFunctions.checkColumn(inSpecs, INPORT_ALL_GENES, ColumnSpecification.GENE_ID, 
    			ColumnSpecification.GENE_ID_TYPE, null);

        return new DataTableSpec[]{TableProcessorMetaboToGeno.generateOutputSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_modeAnno.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_modeAnno.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	m_modeAnno.validateSettings(settings);
    	
    	//this should never happen
    	String mode = settings.getString(CFGKEY_ANNOTATION_MODE);
    	if(!mode.equals(ANNOTATION_MODE_MAX) && !mode.equals(ANNOTATION_MODE_MULTIPLE)){
    		throw new InvalidSettingsException(
    				"Gene Annotation Mode \""+mode+"\" is not supported by this node!");
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}

