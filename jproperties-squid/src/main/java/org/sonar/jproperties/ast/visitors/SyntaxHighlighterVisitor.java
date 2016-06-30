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
package org.sonar.jproperties.ast.visitors;

import com.google.common.collect.ImmutableMap;
import com.sonar.sslr.api.*;

import java.nio.charset.Charset;
import java.util.Map;
import javax.annotation.Nullable;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.jproperties.parser.JavaPropertiesGrammar;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.sslr.parser.LexerlessGrammar;

public class SyntaxHighlighterVisitor extends SquidAstVisitor<LexerlessGrammar> implements AstAndTokenVisitor {

  private static final Map<AstNodeType, TypeOfText> TYPES = ImmutableMap.<AstNodeType, TypeOfText>builder()
    .put(JavaPropertiesGrammar.KEY, TypeOfText.KEYWORD)
    .put(JavaPropertiesGrammar.ELEMENT, TypeOfText.PREPROCESS_DIRECTIVE)
    .build();

  private final SensorContext sensorContext;
  private final Charset charset;
  private final FileSystem fileSystem;
  private NewHighlighting highlighting;
  private SourceFileOffsets sourceFileOffsets;

  public SyntaxHighlighterVisitor(SensorContext sensorContext, Charset charset) {
    this.sensorContext = sensorContext;
    fileSystem = sensorContext.fileSystem();
    this.charset = charset;
  }

  @Override
  public void init() {
    TYPES.keySet()
      .stream()
      .forEach(t -> subscribeTo(t));
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    if (astNode == null) {
      // parse error
      return;
    }
    highlighting = sensorContext.newHighlighting().onFile(fileSystem.inputFile(fileSystem.predicates().is(getContext().getFile())));
    sourceFileOffsets = new SourceFileOffsets(getContext().getFile(), charset);
  }

  @Override
  public void visitNode(AstNode astNode) {
    highlighting
      .highlight(
        sourceFileOffsets.startOffset(astNode),
        sourceFileOffsets.endOffset(astNode),
        TYPES.get(astNode.getType()));
  }

  @Override
  public void visitToken(Token token) {
    token.getTrivia()
      .stream()
      .filter(Trivia::isComment)
      .forEach(t -> highlighting
        .highlight(
          sourceFileOffsets.startOffset(t.getToken()),
          sourceFileOffsets.endOffset(t.getToken()),
          TypeOfText.COMMENT));
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    if (astNode == null) {
      // parse error
      return;
    }
    highlighting.save();
  }

}
