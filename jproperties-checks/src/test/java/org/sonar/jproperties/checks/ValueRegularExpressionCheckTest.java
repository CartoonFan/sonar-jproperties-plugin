/*
 * SonarQube Java Properties Plugin
 * Copyright (C) 2015-2016 David RACODON
 * david.racodon@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.jproperties.checks;

import java.io.File;

import org.junit.Test;
import org.sonar.jproperties.JavaPropertiesAstScanner;
import org.sonar.jproperties.checks.verifier.JavaPropertiesCheckVerifier;

public class ValueRegularExpressionCheckTest {

  private ValueRegularExpressionCheck check = new ValueRegularExpressionCheck();

  @Test
  public void should_match_some_values_and_raise_issues() {
    check.regularExpression = "^(?s)myvalue.*";
    check.message = "Find out values starting with 'myvalue'";
    JavaPropertiesCheckVerifier.verify(check, new File("src/test/resources/checks/valueRegularExpression.properties"));
  }

  @Test
  public void should_not_match_any_keys_and_not_raise_issues() {
    check.regularExpression = "^blabla.*";
    check.message = "Find out values starting with 'blabla'";
    JavaPropertiesCheckVerifier.verify(check, new File("src/test/resources/checks/valueRegularExpressionNoMatch.properties"));
  }

  @Test(expected = IllegalStateException.class)
  public void should_throw_an_illegal_state_exception_as_the_regular_expression_parameter_is_not_valid() {
    check.regularExpression = "(";
    check.message = "blabla";
    JavaPropertiesAstScanner.scanSingleFile(new File("src/test/resources/checks/valueRegularExpressionNoMatch.properties"), check);
  }

}