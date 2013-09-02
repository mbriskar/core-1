/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.addon.validation.ui;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.validate.UIValidator;

/**
 * A {@link UIValidator} adapter that validates using Bean Validation 1.1
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class UIValidationAdapter implements UIValidator
{
   private final Validator validator;
   private final InputComponent<?, ?> input;
   private final Class<UICommand> commandClass;

   public UIValidationAdapter(Validator validator, InputComponent<?, ?> input, Class<UICommand> commandClass)
   {
      super();
      this.validator = validator;
      this.input = input;
      this.commandClass = commandClass;
   }

   @Override
   public void validate(UIValidationContext context)
   {
      Set<ConstraintViolation<UICommand>> violations = validator.validateValue(commandClass, input.getName(), input);
      for (ConstraintViolation<UICommand> violation : violations)
      {
         context.addValidationError(input, violation.getMessage());
      }
   }
}
