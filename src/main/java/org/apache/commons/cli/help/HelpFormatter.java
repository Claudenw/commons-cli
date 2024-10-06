/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.apache.commons.cli.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Util;

/**
 * A default formatter implementation for standard usage.
 * <p>
 * Example:
 * </p>
 * <pre>
 * Options options = new Options();
 * options.addOption(OptionBuilder.withLongOpt("file").withDescription("The file to be processed").hasArg().withArgName("FILE").isRequired().create('f'));
 * options.addOption(OptionBuilder.withLongOpt("version").withDescription("Print the version of the application").create('v'));
 * options.addOption(OptionBuilder.withLongOpt("help").create('h'));
 *
 * String header = "Do something useful with an input file";
 * String footer = "Please report issues at https://example.com/issues";
 *
 * HelpFormatter formatter = new HelpFormatter();
 * formatter.printHelp("myapp", header, options, footer, true);
 * </pre>
 * <p>
 * This produces the following output:
 * </p>
 * <pre>
 *     {@code
 * usage: myapp -f <FILE> [-h] [-v]
 * Do something useful with an input file
 *
 *  -f,--file <FILE>   The file to be processed
 *  -h,--help
 *  -v,--version       Print the version of the application
 *
 * Please report issues at https://example.com/issues
 * }
 * </pre>
 * @since 1.10.0
 */
public class HelpFormatter extends AbstractHelpFormatter {
    /** Default number of characters per line */
    public static final int DEFAULT_WIDTH = 74;

    /** Default padding to the left of each line */
    public static final int DEFAULT_LEFT_PAD = 1;

    /** Number of space characters to be prefixed to each description line */
    public static final int DEFAULT_INDENT = 3;

    /** The default number of spaces between columns in the options table */
    public static final int DEFAULT_COLUMN_SPACING = 5;

    /** If {@code true} show the "Since" column, otherwise ignore it. */
    private final boolean showSince;

    /**
     * A builder for the HelpFormatter.  Intended to make more complex uses of the HelpFormatter class easier.
     * Default values are:
     * <ul>
     *     <li>showSince = true</li>
     *     <li>helpWriter = a {@link TextHelpWriter} writing to {@code System.out}</li>
     *     <li>optionFormatter.Builder = the default {@link OptionFormatter.Builder}</li>
     * </ul>
     */
    public static class Builder {
        /** If {@code true} show the "Since" column, otherwise ignore it. */
        private boolean showSince;
        /** The {@link HelpWriter} to use */
        private HelpWriter helpWriter;
        /** The {@link OptionFormatter.Builder} to use to format options in the table. */
        private OptionFormatter.Builder optionFormatBuilder;
        /** The string to separate option groups with */
        private String optionGroupSeparator;
        /** The comparator to sort lists of options */
        private Comparator<Option> comparator;
        /**
         * Constructor.
         * <p>sets the showSince to {@code true}</p>
         */
        public Builder() {
            showSince = true;
            comparator = DEFAULT_COMPARATOR;
            optionGroupSeparator = DEFAULT_OPTION_GROUP_SEPARATOR;
        }

        /**
         * Sets the showSince flag.
         * @param showSince the desired value of the showSince flag.
         * @return this.
         */
        public Builder setShowSince(final boolean showSince) {
            this.showSince = showSince;
            return this;
        }

        /**
         * Sets the {@link HelpWriter}.
         * @param helpWriter the {@link HelpWriter} to use.
         * @return this
         */
        public Builder setSerializer(final HelpWriter helpWriter) {
            this.helpWriter = helpWriter;
            return this;
        }

        /**
         * Sets the {@link OptionFormatter.Builder}.
         * @param optionFormatBuilder the {@link OptionFormatter.Builder} to use.
         * @return this
         */
        public Builder setOptionFormatBuilder(final OptionFormatter.Builder optionFormatBuilder) {
            this.optionFormatBuilder = optionFormatBuilder;
            return this;
        }

        /**
         * Sets the OptionGroup separator.  Normally " | " or something similar to denote that only one option may
         * be chosen.
         * @param optionGroupSeparator the string to separate option group elements with.
         * @return this
         */
        public Builder setOptionGroupSeparator(final String optionGroupSeparator) {
            this.optionGroupSeparator = Util.defaultValue(optionGroupSeparator, "");
            return this;
        }

        /**
         * Sets the comparator to use for sorting opitons.  If set to {@code null} no sorting is performed.
         * @param comparator The comparator to use for sorting opitons.
         * @return this
         */
        public Builder setComparator(final Comparator<Option> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Performs a sanity check and sets default values if they are not set.
         * @return this.
         */
        private Builder validate() {
            if (helpWriter == null) {
                helpWriter = new TextHelpWriter(System.out);
            }
            if (optionFormatBuilder == null) {
                optionFormatBuilder = new OptionFormatter.Builder();
            }
            return this;
        }

        /**
         * Constructs the {@link HelpFormatter}.
         * @return this.
         */
        public HelpFormatter build() {
            validate();
            return new HelpFormatter(this);
        }
    }

    /**
     * Constructs a new instance using the default {@link Builder}.
     * @see Builder
     */
    public HelpFormatter() {
        this(new Builder().validate());
    }

    /**
     * Convenience constructor to create an instance using the specified {@link HelpWriter} and the
     * remaining default {@link Builder}.
     * @param helpWriter the {@link HelpWriter} to use.
     */
    public HelpFormatter(final HelpWriter helpWriter) {
        this(new Builder().setSerializer(helpWriter).validate());
    }

    /**
     * Constructs the Help formatter.
     * @param builder the Builder to build from.
     */
    private HelpFormatter(final Builder builder) {
        super(builder.helpWriter, builder.optionFormatBuilder, builder.comparator,
                builder.optionGroupSeparator);

        this.showSince = builder.showSince;
    }

    /**
     * Gets the table definition for the options.
     * @param options the collection of {@link Option} instances to create the table from.
     * @return A {@link TableDefinition} to display the options.
     */
    public TableDefinition getTableDefinition(final Iterable<Option> options) {
        // set up the base TextStyle for the columns configured for the Option opt and arg values..
        TextStyle.Builder builder = new TextStyle.Builder().setAlignment(TextStyle.Alignment.LEFT)
                .setIndent(DEFAULT_LEFT_PAD).setScaling(TextStyle.Scaling.UNSCALED);
        List<TextStyle> styles = new ArrayList<>();
        styles.add(builder.get());
        // set up showSince column
        builder.setScaling(TextStyle.Scaling.SCALED).setLeftPad(DEFAULT_COLUMN_SPACING);
        if (showSince) {
            builder.setAlignment(TextStyle.Alignment.CENTER);
            styles.add(builder.get());
        }
        // set up the description column.
        builder.setAlignment(TextStyle.Alignment.LEFT);
        styles.add(builder.get());

        // setup the rows for the table.
        List<List<String>> rows = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Option option : options) {
            List<String> row = new ArrayList<>();
            // create an option formatter to correctly format the parts of the option
            OptionFormatter formatter = optionFormatBuilder.build(option);
            sb.setLength(0);
            // append the opt values.
            sb.append(formatter.getBothOpt());
            // append the arg name if it exists.
            if (option.hasArg()) {
                sb.append(" ").append(formatter.getArgName());
            }
            row.add(sb.toString());
            // append the "since" value if desired.
            if (showSince) {
                row.add(formatter.getSince());
            }
            // add the option description
            row.add(formatter.getDescription());
            rows.add(row);
        }

        // return the TableDefinition with the proper column headers.
        return showSince ? TableDefinition.from("", styles, Arrays.asList("Options", "Since", "Description"), rows) :
                TableDefinition.from("", styles, Arrays.asList("Options", "Description"), rows);
    }
}